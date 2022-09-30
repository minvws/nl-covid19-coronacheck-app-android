/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity

interface ShowBlockedEventsDialogUseCase {
    suspend fun execute(blockedRemoteEvents: List<RemoteEvent>): ShowBlockedEventsDialogResult
}

class ShowBlockedEventsDialogUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : ShowBlockedEventsDialogUseCase {

    override suspend fun execute(blockedRemoteEvents: List<RemoteEvent>): ShowBlockedEventsDialogResult {
        return if (blockedRemoteEvents.isEmpty()) {
            ShowBlockedEventsDialogResult.None
        } else {
            ShowBlockedEventsDialogResult.Show(
                blockedEvents = holderDatabase.blockedEventDao().getAll()
            )
        }
    }
}

sealed class ShowBlockedEventsDialogResult {
    data class Show(val blockedEvents: List<BlockedEventEntity>) : ShowBlockedEventsDialogResult()
    object None : ShowBlockedEventsDialogResult()
}
