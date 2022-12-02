/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events.usecases

import android.app.Application
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTime
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.saved_events.SavedEvents
import nl.rijksoverheid.ctr.holder.your_events.utils.EventGroupEntityUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import org.json.JSONObject

interface GetSavedEventsUseCase {
    suspend fun getSavedEvents(): List<SavedEvents>
}

class GetSavedEventsUseCaseImpl(
    private val application: Application,
    private val holderDatabase: HolderDatabase,
    private val remoteEventUtil: RemoteEventUtil,
    private val eventGroupEntityUtil: EventGroupEntityUtil,
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val infoScreenUtil: InfoScreenUtil,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil
) : GetSavedEventsUseCase {

    override suspend fun getSavedEvents(): List<SavedEvents> {
        val eventGroups = holderDatabase.eventGroupDao().getAll().asReversed()

        return eventGroups.map { eventGroupEntity ->
            val isDccEvent = remoteEventUtil.isDccEvent(
                providerIdentifier = eventGroupEntity.providerIdentifier
            )
            val remoteProtocol = getRemoteProtocolFromEventGroupUseCase.get(eventGroupEntity)

            val fullName = yourEventsFragmentUtil.getFullName(remoteProtocol?.holder)
            val birthDate = yourEventsFragmentUtil.getBirthDate(remoteProtocol?.holder)

            val providerName = eventGroupEntityUtil.getProviderName(
                providerIdentifier = eventGroupEntity.providerIdentifier
            )
            SavedEvents(
                providerName = providerName,
                eventGroupEntity = eventGroupEntity,
                events = remoteProtocol?.events?.mapNotNull { remoteEvent ->
                    val europeanCredential = if (isDccEvent) {
                        JSONObject(eventGroupEntity.jsonData.decodeToString()).getString("credential").toByteArray()
                    } else null

                    val infoScreen = when (remoteEvent) {
                        is RemoteEventVaccination -> {
                            infoScreenUtil.getForVaccination(
                                event = remoteEvent,
                                fullName = fullName,
                                birthDate = birthDate,
                                providerIdentifier = providerName,
                                europeanCredential = europeanCredential,
                                addExplanation = false
                            )
                        }
                        is RemoteEventRecovery -> {
                            infoScreenUtil.getForRecovery(
                                event = remoteEvent,
                                testDate = remoteEvent.recovery?.sampleDate?.formatDayMonthYear()
                                    ?: "",
                                fullName = fullName,
                                birthDate = birthDate,
                                europeanCredential = europeanCredential,
                                addExplanation = false
                            )
                        }
                        is RemoteEventPositiveTest -> {
                            infoScreenUtil.getForPositiveTest(
                                event = remoteEvent,
                                testDate = remoteEvent.positiveTest?.sampleDate?.formatDayMonthYearTime(
                                    application
                                ) ?: "",
                                fullName = fullName,
                                birthDate = birthDate
                            )
                        }
                        is RemoteEventNegativeTest -> {
                            infoScreenUtil.getForNegativeTest(
                                event = remoteEvent,
                                fullName = fullName,
                                testDate = remoteEvent.negativeTest?.sampleDate?.formatDateTime(
                                    application
                                ) ?: "",
                                birthDate = birthDate,
                                europeanCredential = europeanCredential,
                                addExplanation = false
                            )
                        }
                        is RemoteEventVaccinationAssessment -> {
                            infoScreenUtil.getForVaccinationAssessment(
                                event = remoteEvent,
                                fullName = fullName,
                                birthDate = birthDate
                            )
                        }
                        else -> {
                            null
                        }
                    }

                    if (infoScreen != null) {
                        SavedEvents.SavedEvent(
                            remoteEvent = remoteEvent,
                            infoScreen = infoScreen
                        )
                    } else {
                        null
                    }
                }?.asReversed() ?: listOf()
            )
        }
    }
}
