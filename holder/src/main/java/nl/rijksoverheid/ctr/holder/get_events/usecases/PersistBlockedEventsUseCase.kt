/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

interface PersistBlockedEventsUseCase {
    suspend fun persist(newEvents: List<RemoteEvent>, blockedEvents: List<RemoteEvent>)
}

class PersistBlockedEventsUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : PersistBlockedEventsUseCase {

    override suspend fun persist(newEvents: List<RemoteEvent>, blockedEvents: List<RemoteEvent>) {
        val eventsToPersist = blockedEvents.filter { !newEvents.contains(it) }
        eventsToPersist.forEach { remoteEvent ->
            val type = when (remoteEvent) {
                is RemoteEventVaccination -> OriginType.Vaccination
                is RemoteEventPositiveTest -> OriginType.Recovery
                is RemoteEventNegativeTest -> OriginType.Test
                is RemoteEventVaccinationAssessment -> OriginType.VaccinationAssessment
                else -> return
            }

            val blockedEventEntity = BlockedEventEntity(
                walletId = 1,
                type = type,
                eventTime = remoteEvent.getDate()
            )

            holderDatabase.blockedEventDao().insert(blockedEventEntity)
        }
    }
}
