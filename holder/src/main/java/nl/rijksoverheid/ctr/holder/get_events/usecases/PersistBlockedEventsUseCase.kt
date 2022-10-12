/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason

interface PersistBlockedEventsUseCase {
    suspend fun persist(
        newEvents: List<RemoteEvent>,
        removedEvents: List<RemoteEvent>,
        reason: RemovedEventReason
    )
}

class PersistBlockedEventsUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : PersistBlockedEventsUseCase {

    override suspend fun persist(
        newEvents: List<RemoteEvent>,
        removedEvents: List<RemoteEvent>,
        reason: RemovedEventReason
    ) {
        val eventsToPersist = removedEvents.filter { !newEvents.contains(it) }
        eventsToPersist.forEach { remoteEvent ->
            val removedEventEntity = RemovedEventEntity(
                walletId = 1,
                type = remoteEvent.type ?: "",
                eventTime = remoteEvent.getDate(),
                reason = reason
            )

            holderDatabase.blockedEventDao().insert(removedEventEntity)
        }
    }
}
