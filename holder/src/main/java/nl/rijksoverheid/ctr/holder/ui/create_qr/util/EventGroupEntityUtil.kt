package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface EventGroupEntityUtil {
    suspend fun amountOfVaccinationEvents(eventGroupEntities: List<EventGroupEntity>): Int
}

class EventGroupEntityUtilImpl(
    private val remoteEventUtil: RemoteEventUtil
): EventGroupEntityUtil {
    override suspend fun amountOfVaccinationEvents(eventGroupEntities: List<EventGroupEntity>): Int {
        try {
            // DCC only has 1 vaccination event inside the group
            val dccVaccinationEvents = eventGroupEntities
                .filter { it.providerIdentifier == RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC }
                .filter { it.type == OriginType.Vaccination }

            // Non DCC can have multiple vaccinations inside the group (get it from the stored json)
            val nonDccVaccinationEvents = eventGroupEntities
                .asSequence()
                .filter { it.providerIdentifier != RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC }
                .filter { it.type == OriginType.Vaccination }
                .map { remoteEventUtil.getRemoteEventsFromNonDcc(it) }
                .flatten()
                .filterIsInstance<RemoteEventVaccination>()
                .distinctBy { it.vaccination?.date }
                .toList()

            return dccVaccinationEvents.size + nonDccVaccinationEvents.size
        } catch (e: Exception) {
            // If something is wrong with getting this data, make sure the app does not crash
            return 0
        }
    }
}