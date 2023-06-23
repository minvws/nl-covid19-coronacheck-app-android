/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.usecases

import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItems
import nl.rijksoverheid.ctr.holder.dashboard.util.CardItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemEmptyStateUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginUtil
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard

interface GetDashboardItemsUseCase {
    suspend fun getItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success(),
        isLoadingNewCredentials: Boolean
    ): DashboardItems
}

class GetDashboardItemsUseCaseImpl(
    private val greenCardUtil: GreenCardUtil,
    private val credentialUtil: CredentialUtil,
    private val originUtil: OriginUtil,
    private val dashboardItemUtil: DashboardItemUtil,
    private val dashboardItemEmptyStateUtil: DashboardItemEmptyStateUtil,
    private val dashboardHeaderAdapterItemUtil: DashboardHeaderAdapterItemUtil,
    private val cardItemUtil: CardItemUtil,
    private val sortGreenCardItemsUseCase: SortGreenCardItemsUseCase,
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val holderDatabase: HolderDatabase
) : GetDashboardItemsUseCase {
    override suspend fun getItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean
    ): DashboardItems {
        return DashboardItems(
            domesticItems = emptyList(),
            internationalItems = getInternationalItems(
                allGreenCards = allGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                allEventGroupEntities = allEventGroupEntities
            )
        )
    }

    private suspend fun getInternationalItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val internationalGreenCards =
            allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Eu }

        val hasEmptyState = dashboardItemEmptyStateUtil.hasEmptyState(
            allGreenCards = allGreenCards,
            greenCardsForTab = internationalGreenCards
        )

        val headerItem = dashboardHeaderAdapterItemUtil.getHeaderItem(
            emptyState = hasEmptyState,
            greenCardType = GreenCardType.Eu
        )

        dashboardItems.add(headerItem)

        if (dashboardItemUtil.isAppUpdateAvailable()) {
            dashboardItems.add(DashboardItem.InfoItem.AppUpdate)
        }

        if (dashboardItemUtil.shouldShowBlockedEventsItem()) {
            dashboardItems.add(
                DashboardItem.InfoItem.BlockedEvents(
                    blockedEvents = holderDatabase.removedEventDao()
                        .getAll(reason = RemovedEventReason.Blocked)
                )
            )
        }

        if (dashboardItemUtil.shouldShowFuzzyMatchedEventsItem()) {
            dashboardItems.add(
                DashboardItem.InfoItem.FuzzyMatchedEvents(
                    storedEvent = holderDatabase.eventGroupDao().getAll().first(),
                    events = holderDatabase.removedEventDao()
                        .getAll(reason = RemovedEventReason.FuzzyMatched)
                )
            )
        }

        if (dashboardItemUtil.shouldShowClockDeviationItem(hasEmptyState, allGreenCards)) {
            dashboardItems.add(DashboardItem.InfoItem.ClockDeviationItem)
        }

        if (dashboardItemUtil.shouldShowConfigFreshnessWarning()) {
            dashboardItems.add(
                DashboardItem.InfoItem.ConfigFreshnessWarning(
                    maxValidityDate = dashboardItemUtil.getConfigFreshnessMaxValidity()
                )
            )
        }

        if (dashboardItemUtil.shouldShowExportPdf()) {
            dashboardItems.add(
                DashboardItem.InfoItem.ExportPdf()
            )
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCards = allGreenCards,
                greenCardType = GreenCardType.Eu,
                greenCardsForSelectedType = internationalGreenCards,
                greenCardsForUnselectedType = emptyList(),
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                combineVaccinations = true
            )
        )

        if (dashboardItemUtil.shouldShowPlaceholderItem(hasEmptyState)) {
            dashboardItems.add(
                DashboardItem.PlaceholderCardItem(greenCardType = GreenCardType.Eu)
            )
        }

        val addEventsButtonEnabled = featureFlagUseCase.getAddEventsButtonEnabled()

        if (addEventsButtonEnabled) {
            if (dashboardItemUtil.shouldShowAddQrCardItem(false, hasEmptyState)) {
                dashboardItems.add(DashboardItem.AddQrCardItem)
            }

            if (dashboardItemUtil.shouldAddQrButtonItem(hasEmptyState)) {
                dashboardItems.add(DashboardItem.AddQrButtonItem)
            }
        }

        return sortGreenCardItemsUseCase.sort(dashboardItems)
    }

    private fun getGreenCardItems(
        greenCards: List<GreenCard>,
        greenCardType: GreenCardType,
        greenCardsForSelectedType: List<GreenCard>,
        greenCardsForUnselectedType: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean,
        combineVaccinations: Boolean
    ): List<DashboardItem> {

        // Loop through all green cards that exists in the database and map them to UI models
        val items = greenCardsForSelectedType
            .mapIndexed { index, greenCard ->
                if (!featureFlagUseCase.isInArchiveMode() && greenCardUtil.isExpired(greenCard) && greenCard.origins.isNotEmpty()) {
                    getExpiredBannerItem(
                        greenCard = greenCard
                    )
                } else {
                    mapGreenCardsItem(
                        greenCard = greenCard,
                        greenCardIndex = index,
                        isLoadingNewCredentials = isLoadingNewCredentials,
                        databaseSyncerResult = databaseSyncerResult
                    )
                }
            }
            .let { if (combineVaccinations) dashboardItemUtil.combineEuVaccinationItems(it) else it }
            .toMutableList()

        // If we have valid origins that exists in the other selected type but not in the current one, we show a banner
        val allOriginsForSelectedType = greenCardsForSelectedType.map { it.origins }.flatten()
        val allOriginsForUnselectedType = greenCardsForUnselectedType.map { it.origins }.flatten()
        val allValidOriginsForSelectedType = originUtil.getOriginState(allOriginsForSelectedType)
            .filter { it is OriginState.Valid || it is OriginState.Future }.map { it.origin }
        val allValidOriginsForUnselectedType =
            originUtil.getOriginState(allOriginsForUnselectedType)
                .filter { it is OriginState.Valid || it is OriginState.Future }.map { it.origin }

        allValidOriginsForUnselectedType.forEach { originForUnselectedType ->
            if (!allValidOriginsForSelectedType.map { it.type }
                    .contains(originForUnselectedType.type)) {

                if (dashboardItemUtil.shouldShowOriginInfoItem(
                        greenCards = greenCards,
                        greenCardType = greenCardType,
                        originType = originForUnselectedType.type
                    )
                ) {
                    items.add(
                        DashboardItem.InfoItem.OriginInfoItem(
                            greenCardType = greenCardType,
                            originType = originForUnselectedType.type
                        )
                    )
                }
            }
        }

        return items
    }

    private fun getExpiredBannerItem(
        greenCard: GreenCard
    ): DashboardItem {
        val origin = greenCard.origins.last()
        return DashboardItem.InfoItem.GreenCardExpiredItem(
            greenCardType = greenCard.greenCardEntity.type,
            originEntity = origin
        )
    }

    private fun mapGreenCardsItem(
        greenCard: GreenCard,
        greenCardIndex: Int,
        isLoadingNewCredentials: Boolean,
        databaseSyncerResult: DatabaseSyncerResult
    ): DashboardItem.CardsItem {
        // Check if we have a credential
        val activeCredential = credentialUtil.getActiveCredential(
            greenCardType = greenCard.greenCardEntity.type,
            entities = greenCard.credentialEntities
        )

        // Check the states of our origins
        val originStates = originUtil.getOriginState(
            origins = greenCard.origins
        ).sortedBy { it.origin.type.order }

        // Check if we have any valid origins
        val hasValidOriginStates = originStates.any { it is OriginState.Valid }
        val nonExpiredOriginStates = originStates.filterNot { it is OriginState.Expired }

        // More our credential to a more readable state
        val credentialState = when {
            isLoadingNewCredentials -> DashboardItem.CardsItem.CredentialState.LoadingCredential
            activeCredential == null -> DashboardItem.CardsItem.CredentialState.NoCredential
            !hasValidOriginStates && !featureFlagUseCase.isInArchiveMode() -> DashboardItem.CardsItem.CredentialState.NoCredential
            else -> DashboardItem.CardsItem.CredentialState.HasCredential(activeCredential)
        }

        val greenCardItem = DashboardItem.CardsItem.CardItem(
            greenCard = greenCard,
            originStates = if (featureFlagUseCase.isInArchiveMode()) {
                originStates
            } else {
                nonExpiredOriginStates
            },
            credentialState = credentialState,
            databaseSyncerResult = databaseSyncerResult,
            greenCardEnabledState = cardItemUtil.getEnabledState(
                greenCard = greenCard
            )
        )

        return DashboardItem.CardsItem(listOf(greenCardItem))
    }
}
