package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
import nl.rijksoverheid.ctr.shared.livedata.Event
import timber.log.Timber

abstract class DashboardViewModel : ViewModel() {
    open val dashboardTabItems: LiveData<List<DashboardTabItem>> = MutableLiveData()
    open val databaseSyncerResultLiveData: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()

    abstract fun refresh(forceSync: Boolean = false)
}

class DashboardViewModelImpl(
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase,
    private val persistenceManager: PersistenceManager,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase,
    private val clockDeviationUseCase: ClockDeviationUseCase
): DashboardViewModel() {

    private val mutex = Mutex()

    override fun refresh(forceSync: Boolean) {
        viewModelScope.launch {
            mutex.withLock {
                removeExpiredEventsUseCase.execute()

                // Check if we need to refresh our data
                val hasDoneRefreshCall = databaseSyncerResultLiveData.value?.peekContent() != null
                val shouldRefresh = (forceSync) || (greenCardRefreshUtil.shouldRefresh() && !hasDoneRefreshCall)
                val hasClockDeviation = clockDeviationUseCase.calculateDeviationState()

                (dashboardTabItems as MutableLiveData<List<DashboardTabItem>>).postValue(
                    getDashboardTabItems(
                        databaseSyncerResult = databaseSyncerResultLiveData.value?.peekContent()
                            ?: DatabaseSyncerResult.Success,
                        hasClockDeviation = hasClockDeviation,
                        shouldRefresh = shouldRefresh
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
                    dashboardTabItems.postValue(
                        getDashboardTabItems(
                            databaseSyncerResult = databaseSyncerResultLiveData.value?.peekContent() ?: DatabaseSyncerResult.Success,
                            hasClockDeviation = hasClockDeviation,
                            shouldRefresh = false
                        )
                    )
                }
            }
        }
    }

    private suspend fun getDashboardTabItems(
        databaseSyncerResult: DatabaseSyncerResult,
        hasClockDeviation: Boolean,
        shouldRefresh: Boolean
    ): List<DashboardTabItem> {
        val domesticItem = DashboardTabItem(
            title = R.string.travel_button_domestic,
            items = getMyOverviewItemsUseCase.get(
                selectedType = GreenCardType.Domestic,
                walletId = 1,
                databaseSyncerResult = databaseSyncerResult,
                shouldRefresh = shouldRefresh,
                hasClockDeviation = hasClockDeviation
            ).items,
            greenCardType = GreenCardType.Domestic
        )

        val internationalItem = DashboardTabItem(
            title = R.string.travel_button_europe,
            items = getMyOverviewItemsUseCase.get(
                selectedType = GreenCardType.Eu,
                walletId = 1,
                databaseSyncerResult = databaseSyncerResult,
                shouldRefresh = shouldRefresh,
                hasClockDeviation = hasClockDeviation
            ).items,
            greenCardType = GreenCardType.Eu
        )

        return listOf(
            domesticItem,
            internationalItem
        )
    }
}