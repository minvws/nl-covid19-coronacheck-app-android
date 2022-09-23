/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.usecases

import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity

interface ShowBlockedEventsDialogUseCase {
    suspend fun execute(): ShowBlockedEventsDialogResult
}

class ShowBlockedEventsDialogUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val persistenceManager: PersistenceManager
) : ShowBlockedEventsDialogUseCase {

    override suspend fun execute(): ShowBlockedEventsDialogResult {
        val blockedEvents = holderDatabase.blockedEventDao().getAll()
        val show = persistenceManager.getCanShowBlockedEventsDialog() && blockedEvents.isNotEmpty()
        if (show) {
            persistenceManager.setCanShowBlockedEventsDialog(false)
        }
        return if (show) {
            ShowBlockedEventsDialogResult.Show(blockedEvents)
        } else {
            ShowBlockedEventsDialogResult.None
        }
    }
}

sealed class ShowBlockedEventsDialogResult {
    data class Show(val blockedEvents: List<BlockedEventEntity>) : ShowBlockedEventsDialogResult()
    object None : ShowBlockedEventsDialogResult()
}
