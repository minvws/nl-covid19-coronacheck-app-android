/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.sync_greencards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.AppErrorResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class SyncGreenCardsViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val databaseSyncerResultLiveData: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()

    abstract fun refresh()
}

class SyncGreenCardsViewModelImpl(
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val persistenceManager: PersistenceManager): SyncGreenCardsViewModel() {
    override fun refresh() {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val databaseSyncerResult = holderDatabaseSyncer.sync()
                if (databaseSyncerResult is DatabaseSyncerResult.Success) {
                    persistenceManager.setShowSyncGreenCardsItem(false)
                }
                (databaseSyncerResultLiveData as MutableLiveData).value = Event(databaseSyncerResult)
            } catch (e: Exception) {
                (databaseSyncerResultLiveData as MutableLiveData).value = Event(
                    DatabaseSyncerResult.Failed.Error(AppErrorResult(HolderStep.StoringEvents, e))
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }
}