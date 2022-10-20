package nl.rijksoverheid.ctr.holder.fuzzy_matching

import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface MatchedEventsUseCase {
    suspend fun selected(selectionIndex: Int, matchingBlobIds: List<List<Int>>)
}

class MatchedEventsUseCaseImpl(
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val holderDatabase: HolderDatabase
) : MatchedEventsUseCase {
    override suspend fun selected(selectionIndex: Int, matchingBlobIds: List<List<Int>>) {
        val eventIdsToDelete = matchingBlobIds.filterIndexed { index, _ ->
            index != selectionIndex
        }.flatten().toSet().filter { !matchingBlobIds[selectionIndex].contains(it) }

        holderDatabase.eventGroupDao().getAllOfIds(eventIdsToDelete).forEach { eventGroupEntity ->
            val remoteProtocol = getRemoteProtocolFromEventGroupUseCase.get(eventGroupEntity)
            remoteProtocol?.events?.forEach { remoteEvent ->
                holderDatabase.removedEventDao().insert(
                    RemovedEventEntity(
                        walletId = eventGroupEntity.walletId,
                        type = eventGroupEntity.type.getTypeString(),
                        eventTime = remoteEvent.getDate(),
                        reason = RemovedEventReason.FuzzyMatched
                    )
                )
            }
        }

        holderDatabase.eventGroupDao().deleteAllOfIds(eventIdsToDelete)
    }
}
