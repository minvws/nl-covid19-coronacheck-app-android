package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class MyOverviewViewModel : ViewModel() {
    open val myOverviewItemsLiveData: LiveData<Event<MyOverviewItems>> = MutableLiveData()
    open val databaseSyncerResultLiveData: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()

    abstract fun getSelectedType(): GreenCardType

    /**
     * Refresh all the items we need to display on the overview
     * @param selectType The type of green cards you want to show, null if refresh the current selected one
     */
    abstract fun refreshOverviewItems(
        selectType: GreenCardType = getSelectedType(),
        forceSync: Boolean = false
    )
}

class MyOverviewViewModelImpl(
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase,
    private val persistenceManager: PersistenceManager,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase,
    private val clockDeviationUseCase: ClockDeviationUseCase
) : MyOverviewViewModel() {

    private val mutex = Mutex()

    override fun getSelectedType(): GreenCardType {
        return (myOverviewItemsLiveData.value?.peekContent()?.selectedType
            ?: persistenceManager.getSelectedGreenCardType())
    }

    override fun refreshOverviewItems(selectType: GreenCardType, forceSync: Boolean) {
        // When the app is opened we need to remember the tab that was selected
        persistenceManager.setSelectedGreenCardType(selectType)

        viewModelScope.launch {
            mutex.withLock {
                removeExpiredEventsUseCase.execute()

                // Check if we need to refresh our data
                val hasDoneRefreshCall =
                    databaseSyncerResultLiveData.value?.peekContent() != null && selectType == getSelectedType()
                val shouldRefresh =
                    (forceSync) || (greenCardRefreshUtil.shouldRefresh() && !hasDoneRefreshCall)

                // If domestic, check for deviation, otherwise return stored value
                val hasClockDeviation = if (selectType == GreenCardType.Domestic) {
                    clockDeviationUseCase.calculateDeviationState()
                } else {
                    clockDeviationUseCase.retrieveDeviationState()
                }

                // Get items we need to show on the overview
                (myOverviewItemsLiveData as MutableLiveData).postValue(
                    Event(
                        getMyOverviewItemsUseCase.get(
                            selectedType = selectType,
                            walletId = 1,
                            databaseSyncerResult = databaseSyncerResultLiveData.value?.peekContent()
                                ?: DatabaseSyncerResult.Success,
                            shouldRefresh = shouldRefresh,
                            hasClockDeviation = hasClockDeviation
                        )
                    )
                )

                if (shouldRefresh) {
                    // Communicate refresh to the UI (only once)
                    (databaseSyncerResultLiveData as MutableLiveData).postValue(
                        Event(DatabaseSyncerResult.Loading)
                    )

                    // Refresh the database
                    // This checks if we need to remove expired EventGroupEntity's
                    // Also syncs the database with remote if needed
                    val databaseSyncerResult = holderDatabaseSyncer.sync(
                        syncWithRemote = shouldRefresh
                    )

                    // Communicate refresh to the UI (only once)
                    databaseSyncerResultLiveData.postValue(
                        Event(databaseSyncerResult)
                    )

                    // If we needed to refresh out data we want to refresh the items on the overview again
                    myOverviewItemsLiveData.postValue(
                        Event(
                            getMyOverviewItemsUseCase.get(
                                selectedType = selectType,
                                walletId = 1,
                                databaseSyncerResult = databaseSyncerResult,
                                shouldRefresh = false,
                                hasClockDeviation = hasClockDeviation
                            )
                        )
                    )
                }
            }
        }
    }
}