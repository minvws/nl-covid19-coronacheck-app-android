/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.dashboard.usecases.RemoveExpiredGreenCardsUseCase
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.dashboard.datamappers.DashboardTabsItemDataMapper
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.dashboard.usecases.GetDashboardItemsUseCase
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogResult
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogUseCase
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

abstract class DashboardViewModel : ViewModel() {
    val dashboardTabItemsLiveData: LiveData<List<DashboardTabItem>> = MutableLiveData()
    val databaseSyncerResultLiveData: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()
    val showBlockedEventsDialogLiveData: LiveData<Event<ShowBlockedEventsDialogResult>> = MutableLiveData()

    abstract fun refresh(dashboardSync: DashboardSync = DashboardSync.CheckSync)
    abstract fun removeOrigin(originEntity: OriginEntity)
    abstract fun dismissPolicyInfo(disclosurePolicy: DisclosurePolicy)
    abstract fun dismissBlockedEventsInfo()

    companion object {
        val RETRY_FAILED_REQUEST_AFTER_SECONDS = if (BuildConfig.FLAVOR == "acc") TimeUnit.SECONDS.toSeconds(10) else TimeUnit.MINUTES.toSeconds(10)
    }
}

class DashboardViewModelImpl(
    private val holderDatabase: HolderDatabase,
    private val greenCardUtil: GreenCardUtil,
    private val getDashboardItemsUseCase: GetDashboardItemsUseCase,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val persistenceManager: PersistenceManager,
    private val removeExpiredGreenCardsUseCase: RemoveExpiredGreenCardsUseCase,
    private val dashboardTabsItemDataMapper: DashboardTabsItemDataMapper,
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase,
    private val showBlockedEventsDialogUseCase: ShowBlockedEventsDialogUseCase
) : DashboardViewModel() {

    private val mutex = Mutex()

    /**
     * Refreshing of database happens every 60 seconds
     */
    override fun refresh(dashboardSync: DashboardSync) {
        viewModelScope.launch {
            refreshCredentials(dashboardSync)
        }
    }

    private suspend fun refreshCredentials(dashboardSync: DashboardSync) {
        mutex.withLock {
            val previousSyncResult = databaseSyncerResultLiveData.value?.peekContent()

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
                    val shouldRefreshCredentials = greenCardRefreshUtil.shouldRefresh()

                    // Load new credentials if we the previous request failed more than once and more than x minutes ago
                    val shouldRetryFailedRequest =
                        previousSyncResult is DatabaseSyncerResult.Failed.ServerError.MultipleTimes && OffsetDateTime.now()
                            .isAfter(
                                previousSyncResult.failedAt.plusSeconds(
                                    RETRY_FAILED_REQUEST_AFTER_SECONDS
                                )
                            )

                    // Do the actual checks
                    shouldRefreshCredentials || shouldRetryFailedRequest
                }
            }

            val allGreenCards = greenCardUtil.getAllGreenCards()
            val allEventGroupEntities = holderDatabase.eventGroupDao().getAll()

            removeExpiredGreenCardsUseCase.execute(
                allGreenCards = allGreenCards
            )

            refreshDashboardTabItems(
                allGreenCards = allGreenCards,
                databaseSyncerResult = databaseSyncerResultLiveData.value?.peekContent()
                    ?: DatabaseSyncerResult.Success(listOf()),
                isLoadingNewCredentials = shouldLoadNewCredentials,
                allEventGroupEntities = allEventGroupEntities
            )

            val databaseSyncerResult = holderDatabaseSyncer.sync(
                syncWithRemote = shouldLoadNewCredentials,
                previousSyncResult = previousSyncResult,
                flow = HolderFlow.Refresh
            )

            if (databaseSyncerResult is DatabaseSyncerResult.Success) {
                val result = showBlockedEventsDialogUseCase.execute()
                (showBlockedEventsDialogLiveData as MutableLiveData).postValue(Event(result))
            }

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

            removeExpiredEventsUseCase.execute(
                events = allEventGroupEntities
            )
        }
    }

    /**
     * Remove the origin from a green card.
     */
    override fun removeOrigin(originEntity: OriginEntity) {
        viewModelScope.launch {
            holderDatabase.originDao().delete(originEntity)
        }
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

        val tabItems = dashboardTabsItemDataMapper.transform(
            dashboardItems = items
        )

        (dashboardTabItemsLiveData as MutableLiveData<List<DashboardTabItem>>).postValue(
            tabItems
        )
    }

    override fun dismissPolicyInfo(disclosurePolicy: DisclosurePolicy) {
        persistenceManager.setPolicyBannerDismissed(disclosurePolicy)
    }

    override fun dismissBlockedEventsInfo() {
        viewModelScope.launch {
            persistenceManager.setCanShowBlockedEventsDialog(true)
            holderDatabase.blockedEventDao().deleteAll()
        }
    }
}
