package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItems
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.HeaderItemTextUtil

interface GetDashboardItemsUseCase {
    suspend fun getItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success(),
        isLoadingNewCredentials: Boolean,
    ): DashboardItems
}

class GetDashboardItemsUseCaseImpl(
    private val greenCardUtil: GreenCardUtil,
    private val credentialUtil: CredentialUtil,
    private val originUtil: OriginUtil,
    private val dashboardItemUtil: DashboardItemUtil,
    private val dashboardItemEmptyStateUtil: DashboardItemEmptyStateUtil,
    private val headerItemTextUtil: HeaderItemTextUtil,
    private val cardItemUtil: CardItemUtil,
    private val splitDomesticGreenCardsUseCase: SplitDomesticGreenCardsUseCase,
    private val sortGreenCardItemsUseCase: SortGreenCardItemsUseCase
) : GetDashboardItemsUseCase {
    override suspend fun getItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean
    ): DashboardItems {
        return DashboardItems(
            domesticItems = getDomesticItems(
                allGreenCards = allGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                allEventGroupEntities = allEventGroupEntities
            ),
            internationalItems = getInternationalItems(
                allGreenCards = allGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                allEventGroupEntities = allEventGroupEntities
            )
        )
    }

    private suspend fun getDomesticItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean,
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()

        val domesticGreenCards =
            allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Domestic }

        val hasVisitorPassIncompleteItem = dashboardItemUtil.shouldShowVisitorPassIncompleteItem(
            events = allEventGroupEntities,
            domesticGreenCards = domesticGreenCards
        )
        val hasEmptyState = dashboardItemEmptyStateUtil.hasEmptyState(
            hasVisitorPassIncompleteItem = hasVisitorPassIncompleteItem,
            allGreenCards = allGreenCards
        )

        // Apply distinctBy here so that for two european green cards we do not get a two banners
        // saying "the certificate isn't valid in NL"
        val internationalGreenCards = allGreenCards
            .filter { it.greenCardEntity.type == GreenCardType.Eu }
            .distinctBy { it.greenCardEntity.type }

        val headerText = headerItemTextUtil.getText(
            emptyState = hasEmptyState,
            greenCardType = GreenCardType.Domestic,
            hasVisitorPassIncompleteItem = hasVisitorPassIncompleteItem,
        )

        dashboardItems.add(DashboardItem.HeaderItem(text = headerText))

        if (dashboardItemUtil.isAppUpdateAvailable()) {
            dashboardItems.add(DashboardItem.InfoItem.AppUpdate)
        }

        if (dashboardItemUtil.shouldShowClockDeviationItem(hasEmptyState, allGreenCards)) {
            dashboardItems.add(DashboardItem.InfoItem.ClockDeviationItem)
        }

        if (dashboardItemUtil.shouldShowVisitorPassIncompleteItem(
                events = allEventGroupEntities,
                domesticGreenCards = domesticGreenCards
            )
        ) {
            dashboardItems.add(
                DashboardItem.InfoItem.VisitorPassIncompleteItem
            )
        }

        if (dashboardItemUtil.shouldShowConfigFreshnessWarning()) {
            dashboardItems.add(
                DashboardItem.InfoItem.ConfigFreshnessWarning(
                    maxValidityDate = dashboardItemUtil.getConfigFreshnessMaxValidity()
                )
            )
        }

        if (dashboardItemUtil.shouldShowNewValidityItem()) {
            dashboardItems.add(
                DashboardItem.InfoItem.NewValidityItem
            )
        }

        if (dashboardItemUtil.shouldShowBoosterItem(domesticGreenCards)) {
            dashboardItems.add(
                DashboardItem.InfoItem.BoosterItem
            )
        }

        dashboardItemUtil.showPolicyInfoItem()?.let {
            dashboardItems.add(DashboardItem.InfoItem.DisclosurePolicyItem(it))
        }
        
        dashboardItems.addAll(
            getGreenCardItems(
                greenCards = allGreenCards,
                greenCardType = GreenCardType.Domestic,
                greenCardsForSelectedType = splitDomesticGreenCardsUseCase.getSplitDomesticGreenCards(
                    domesticGreenCards = domesticGreenCards
                ),
                greenCardsForUnselectedType = internationalGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                combineVaccinations = false
            )
        )

        if (dashboardItemUtil.shouldShowPlaceholderItem(hasEmptyState)) {
            dashboardItems.add(
                DashboardItem.PlaceholderCardItem(greenCardType = GreenCardType.Domestic)
            )
        }

        if (dashboardItemUtil.shouldShowAddQrCardItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.AddQrCardItem)
        }

        if (dashboardItemUtil.shouldShowCoronaMelderItem(
                domesticGreenCards,
                databaseSyncerResult
            )
        ) {
            dashboardItems.add(
                DashboardItem.CoronaMelderItem
            )
        }

        if (dashboardItemUtil.shouldAddQrButtonItem(hasEmptyState)) {
            dashboardItems.add(DashboardItem.AddQrButtonItem)
        }

        return dashboardItems
    }

    private suspend fun getInternationalItems(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean,
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val domesticGreenCards =
            allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Domestic }
        val internationalGreenCards =
            allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Eu }

        val hasVisitorPassIncompleteItem = dashboardItemUtil.shouldShowVisitorPassIncompleteItem(
            events = allEventGroupEntities,
            domesticGreenCards = domesticGreenCards
        )
        val hasEmptyState = dashboardItemEmptyStateUtil.hasEmptyState(
            hasVisitorPassIncompleteItem = hasVisitorPassIncompleteItem,
            allGreenCards = allGreenCards,
        )

        val headerText = headerItemTextUtil.getText(
            emptyState = hasEmptyState,
            greenCardType = GreenCardType.Eu,
            hasVisitorPassIncompleteItem = hasVisitorPassIncompleteItem,
        )

        dashboardItems.add(
            DashboardItem.HeaderItem(text = headerText)
        )

        if (dashboardItemUtil.isAppUpdateAvailable()) {
            dashboardItems.add(DashboardItem.InfoItem.AppUpdate)
        }

        if (dashboardItemUtil.shouldShowClockDeviationItem(hasEmptyState, allGreenCards)) {
            dashboardItems.add(DashboardItem.InfoItem.ClockDeviationItem)
        }

        // If the incomplete visitor pass banner shows in domestic, we show the relevant missing origin banner in EU
        if (dashboardItemUtil.shouldShowVisitorPassIncompleteItem(
                events = allEventGroupEntities,
                domesticGreenCards = domesticGreenCards
            )
        ) {

            dashboardItems.add(
                DashboardItem.InfoItem.OriginInfoItem(
                    greenCardType = GreenCardType.Eu,
                    originType = OriginType.VaccinationAssessment
                )
            )
        }

        if (dashboardItemUtil.shouldShowConfigFreshnessWarning()) {
            dashboardItems.add(
                DashboardItem.InfoItem.ConfigFreshnessWarning(
                    maxValidityDate = dashboardItemUtil.getConfigFreshnessMaxValidity()
                )
            )
        }

        if (dashboardItemUtil.shouldShowBoosterItem(domesticGreenCards)) {
            dashboardItems.add(
                DashboardItem.InfoItem.BoosterItem
            )
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCards = allGreenCards,
                greenCardType = GreenCardType.Eu,
                greenCardsForSelectedType = internationalGreenCards,
                greenCardsForUnselectedType = domesticGreenCards,
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

        if (dashboardItemUtil.shouldShowAddQrCardItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.AddQrCardItem)
        }

        if (dashboardItemUtil.shouldShowCoronaMelderItem(
                internationalGreenCards,
                databaseSyncerResult
            )
        ) {
            dashboardItems.add(
                DashboardItem.CoronaMelderItem
            )
        }

        if (dashboardItemUtil.shouldAddQrButtonItem(hasEmptyState)) {
            dashboardItems.add(DashboardItem.AddQrButtonItem)
        }

        return dashboardItems
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
                if (greenCardUtil.isExpired(greenCard) && greenCard.origins.isNotEmpty()) {
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
                    )) {
                    items.add(
                        if (greenCardType == GreenCardType.Domestic
                            && dashboardItemUtil.shouldShowMissingDutchVaccinationItem(
                                greenCardsForSelectedType,
                                greenCardsForUnselectedType
                            )
                        ) {
                            DashboardItem.InfoItem.MissingDutchVaccinationItem
                        } else {
                            DashboardItem.InfoItem.OriginInfoItem(
                                greenCardType = greenCardType,
                                originType = originForUnselectedType.type
                            )
                        }
                    )
                }
            }
        }

        return sortGreenCardItemsUseCase.sort(items)
    }

    private fun getExpiredBannerItem(
        greenCard: GreenCard,
    ): DashboardItem {
        val origin = greenCard.origins.last()
        return when {
            greenCard.greenCardEntity.type is GreenCardType.Domestic && origin.type is OriginType.Vaccination -> {
                DashboardItem.InfoItem.DomesticVaccinationExpiredItem(origin)
            }
            greenCard.greenCardEntity.type is GreenCardType.Domestic && origin.type is OriginType.VaccinationAssessment -> {
                DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem(origin)
            }
            else -> {
                DashboardItem.InfoItem.GreenCardExpiredItem(
                    greenCardType = greenCard.greenCardEntity.type,
                    originEntity = origin
                )
            }
        }
    }

    private fun mapGreenCardsItem(
        greenCard: GreenCard,
        greenCardIndex: Int,
        isLoadingNewCredentials: Boolean,
        databaseSyncerResult: DatabaseSyncerResult
    ): DashboardItem.CardsItem {
        // Check if we have a credential
        val activeCredential = credentialUtil.getActiveCredential(
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
            !hasValidOriginStates -> DashboardItem.CardsItem.CredentialState.NoCredential
            else -> DashboardItem.CardsItem.CredentialState.HasCredential(activeCredential)
        }

        val greenCardItem = DashboardItem.CardsItem.CardItem(
            greenCard = greenCard,
            originStates = nonExpiredOriginStates,
            credentialState = credentialState,
            databaseSyncerResult = databaseSyncerResult,
            disclosurePolicy = cardItemUtil.getDisclosurePolicy(
                greenCardIndex = greenCardIndex,
                greenCard = greenCard
            ),
            greenCardEnabledState = cardItemUtil.getEnabledState(
                greenCard = greenCard
            )
        )

        return DashboardItem.CardsItem(listOf(greenCardItem))
    }
}