package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardErrorState
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetDashboardItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardSync
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
import nl.rijksoverheid.ctr.shared.livedata.Event
import java.time.OffsetDateTime

abstract class DashboardViewModel : ViewModel() {
    open val dashboardTabItemsLiveData: LiveData<List<DashboardTabItem>> = MutableLiveData()
    open val databaseSyncerResultLiveData: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()

    var dashboardErrorState: DashboardErrorState = DashboardErrorState.None

    abstract fun refresh(dashboardSync: DashboardSync = DashboardSync.CheckCredentialsExpired)
    abstract fun removeGreenCard(greenCard: GreenCard)

    companion object {
        internal const val retryIntervalMinutes = 10L
    }
}

class DashboardViewModelImpl(
    private val holderDatabase: HolderDatabase,
    private val getDashboardItemsUseCase: GetDashboardItemsUseCase,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
): DashboardViewModel() {

    private val mutex = Mutex()

    override fun refresh(dashboardSync: DashboardSync) {
        viewModelScope.launch {
            mutex.withLock {
                // Check if we need to refresh our data
                val previousSyncResult = databaseSyncerResultLiveData.value?.peekContent()
                val hasDoneRefreshCall = previousSyncResult != null

                val shouldLoadNewCredentials = when (dashboardSync) {
                    is DashboardSync.ForceSync -> {
                        true
                    }
                    is DashboardSync.DisableSync -> {
                        false
                    }
                    is DashboardSync.CheckCredentialsExpired -> {
                        (greenCardRefreshUtil.shouldRefresh() && !hasDoneRefreshCall)
                    }
                }

                val allGreenCards = holderDatabase.greenCardDao().getAll()

                refreshDashboardTabItems(
                    allGreenCards = allGreenCards,
                    databaseSyncerResult = databaseSyncerResultLiveData.value?.peekContent()
                        ?: DatabaseSyncerResult.Success,
                    isLoadingNewCredentials = shouldLoadNewCredentials
                )

                val databaseSyncerResult = holderDatabaseSyncer.sync(
                    syncWithRemote = shouldLoadNewCredentials,
                    previousSyncResult = previousSyncResult
                )

                (databaseSyncerResultLiveData as MutableLiveData).postValue(
                    Event(databaseSyncerResult)
                )

                // If we loaded new credentials, we want to update our items again
                if (shouldLoadNewCredentials) {
                    // Set the last time we loaded new credentials
                    refreshDashboardTabItems(
                        allGreenCards = allGreenCards,
                        databaseSyncerResult = databaseSyncerResult,
                        isLoadingNewCredentials = false
                    )
                }
            }
        }
    }

    override fun removeGreenCard(greenCard: GreenCard) {
        viewModelScope.launch {
            holderDatabase.greenCardDao().delete(greenCard.greenCardEntity)
        }
    }

    private suspend fun refreshDashboardTabItems(
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean
    ) {
        val items = getDashboardItemsUseCase.getItems(
            allGreenCards = allGreenCards,
            databaseSyncerResult = databaseSyncerResult,
            isLoadingNewCredentials = isLoadingNewCredentials
        )

        val domesticItem = DashboardTabItem(
            title = R.string.travel_button_domestic,
            greenCardType = GreenCardType.Domestic,
            items = items.domesticItems
        )

        val internationalItem = DashboardTabItem(
            title = R.string.travel_button_europe,
            greenCardType = GreenCardType.Eu,
            items = items.internationalItems
        )

        val dashboardTabItems = listOf(
            domesticItem,
            internationalItem
        )

        (dashboardTabItemsLiveData as MutableLiveData<List<DashboardTabItem>>).postValue(
            dashboardTabItems
        )
    }
}