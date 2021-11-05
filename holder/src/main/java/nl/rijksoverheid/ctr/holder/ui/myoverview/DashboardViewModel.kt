package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.CheckNewRecoveryValidityUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetDashboardItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardSync
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
import nl.rijksoverheid.ctr.shared.livedata.Event
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

abstract class DashboardViewModel : ViewModel() {
    open val dashboardTabItemsLiveData: LiveData<List<DashboardTabItem>> = MutableLiveData()
    open val databaseSyncerResultLiveData: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()

    abstract fun refresh(dashboardSync: DashboardSync = DashboardSync.CheckSync)
    abstract fun removeGreenCard(greenCard: GreenCard)
    abstract fun dismissRefreshedEuVaccinationsInfoCard()
    abstract fun dismissRecoveredDomesticRecoveryInfoCard()
    abstract fun dismissExtendedDomesticRecoveryInfoCard()

    companion object {
        val RETRY_FAILED_REQUEST_AFTER_SECONDS = if (BuildConfig.FLAVOR == "acc") TimeUnit.SECONDS.toSeconds(10) else TimeUnit.MINUTES.toSeconds(10)
    }
}

class DashboardViewModelImpl(
    private val holderDatabase: HolderDatabase,
    private val getDashboardItemsUseCase: GetDashboardItemsUseCase,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val persistenceManager: PersistenceManager,
    private val checkNewRecoveryValidityUseCase: CheckNewRecoveryValidityUseCase
): DashboardViewModel() {

    private val mutex = Mutex()

    /**
     * Refreshing of database happens every 60 seconds
     */
    override fun refresh(dashboardSync: DashboardSync) {
        viewModelScope.launch {
            mutex.withLock {
                checkNewRecoveryValidityUseCase.check()

                val previousSyncResult = databaseSyncerResultLiveData.value?.peekContent()
                val hasDoneRefreshCall = previousSyncResult != null

                // Check if we need to load new credentials
                val shouldLoadNewCredentials = when (dashboardSync) {
                    is DashboardSync.ForceSync -> {
                        // Load new credentials if we force it. For example on a retry button click
                        true
                    }
                    is DashboardSync.DisableSync -> {
                        // Never load new credentials when we don't want to. For example if we are checking to show the clock skew banner
                        false
                    }
                    is DashboardSync.CheckSync -> {
                        // Load new credentials if no previous refresh has been executed and we should refresh because a credentials for a green card expired
                        val shouldRefreshCredentials = (greenCardRefreshUtil.shouldRefresh() && !hasDoneRefreshCall)

                        // Load new credentials if we the previous request failed more than once and more than x minutes ago
                        val shouldRetryFailedRequest = previousSyncResult is DatabaseSyncerResult.Failed.ServerError.MultipleTimes && OffsetDateTime.now().isAfter(previousSyncResult.failedAt.plusSeconds(RETRY_FAILED_REQUEST_AFTER_SECONDS))

                        // Do the actual checks
                        shouldRefreshCredentials || shouldRetryFailedRequest
                    }
                }

                val allGreenCards = holderDatabase.greenCardDao().getAll()
                val allEventGroupEntities = holderDatabase.eventGroupDao().getAll()

                refreshDashboardTabItems(
                    allGreenCards = allGreenCards,
                    databaseSyncerResult = databaseSyncerResultLiveData.value?.peekContent()
                        ?: DatabaseSyncerResult.Success(),
                    isLoadingNewCredentials = shouldLoadNewCredentials,
                    allEventGroupEntities = allEventGroupEntities
                )

                val databaseSyncerResult = holderDatabaseSyncer.sync(
                    syncWithRemote = shouldLoadNewCredentials,
                    previousSyncResult = previousSyncResult
                )

                (databaseSyncerResultLiveData as MutableLiveData).value = Event(databaseSyncerResult)

                // If we loaded new credentials, we want to update our items again
                if (shouldLoadNewCredentials) {
                    refreshDashboardTabItems(
                        allGreenCards = allGreenCards,
                        allEventGroupEntities = allEventGroupEntities,
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

    override fun dismissRefreshedEuVaccinationsInfoCard() {
        persistenceManager.setHasDismissedSyncedGreenCardsItem(true)
    }

    override fun dismissRecoveredDomesticRecoveryInfoCard() {
        persistenceManager.setHasDismissedRecoveredDomesticRecoveryInfoCard(true)
    }

    override fun dismissExtendedDomesticRecoveryInfoCard() {
        persistenceManager.setHasDismissedExtendedDomesticRecoveryInfoCard(true)
    }

    private suspend fun refreshDashboardTabItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean
    ) {
        val items = getDashboardItemsUseCase.getItems(
            allGreenCards = allGreenCards,
            databaseSyncerResult = databaseSyncerResult,
            isLoadingNewCredentials = isLoadingNewCredentials,
            allEventGroupEntities = allEventGroupEntities
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