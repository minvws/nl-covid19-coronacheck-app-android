package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItems
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*

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
    private val dashboardItemEmptyStateUtil: DashboardItemEmptyStateUtil
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

        val hasEmptyState = dashboardItemEmptyStateUtil.hasEmptyState(
            hasVisitorPassIncompleteItem = dashboardItemUtil.shouldShowVisitorPassIncompleteItem(
                events = allEventGroupEntities,
                domesticGreenCards = domesticGreenCards
            ),
            allGreenCards = allGreenCards
        )

        // Apply distinctBy here so that for two european green cards we do not get a two banners
        // saying "the certificate isn't valid in NL"
        val internationalGreenCards = allGreenCards
            .filter { it.greenCardEntity.type == GreenCardType.Eu }
            .distinctBy { it.greenCardEntity.type }

        val headerText = dashboardItemUtil.getHeaderItemText(
            emptyState = hasEmptyState,
            greenCardType = GreenCardType.Domestic,
        )

        dashboardItems.add(DashboardItem.HeaderItem(text = headerText))

        if (dashboardItemUtil.isAppUpdateAvailable()) {
            dashboardItems.add(DashboardItem.InfoItem.AppUpdate)
        }

        if (dashboardItemUtil.shouldShowClockDeviationItem(hasEmptyState, allGreenCards)) {
            dashboardItems.add(DashboardItem.InfoItem.ClockDeviationItem)
        }

        if (dashboardItemUtil.shouldShowExtendDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.ExtendDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowRecoverDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.RecoverDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowRecoveredDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.RecoveredDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowExtendedDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.ExtendedDomesticRecovery)
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

        if (dashboardItemUtil.shouldShowTestCertificate3GValidityItem(domesticGreenCards)) {
            dashboardItems.add(DashboardItem.InfoItem.TestCertificate3GValidity)
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCards = allGreenCards,
                greenCardType = GreenCardType.Domestic,
                greenCardsForSelectedType = domesticGreenCards,
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

        if (dashboardItemUtil.shouldShowCoronaMelderItem(
                domesticGreenCards,
                databaseSyncerResult
            )
        ) {
            dashboardItems.add(
                DashboardItem.CoronaMelderItem
            )
        }

        dashboardItems.add(
            DashboardItem.AddQrButtonItem(dashboardItemUtil.shouldAddQrButtonItem(hasEmptyState))
        )

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

        val hasEmptyState = dashboardItemEmptyStateUtil.hasEmptyState(
            hasVisitorPassIncompleteItem = dashboardItemUtil.shouldShowVisitorPassIncompleteItem(
                events = allEventGroupEntities,
                domesticGreenCards = domesticGreenCards
            ),
            allGreenCards = allGreenCards
        )

        val headerText = dashboardItemUtil.getHeaderItemText(
            emptyState = hasEmptyState,
            greenCardType = GreenCardType.Eu,
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

        if (dashboardItemUtil.shouldShowCoronaMelderItem(
                internationalGreenCards,
                databaseSyncerResult
            )
        ) {
            dashboardItems.add(
                DashboardItem.CoronaMelderItem
            )
        }

        dashboardItems.add(
            DashboardItem.AddQrButtonItem(dashboardItemUtil.shouldAddQrButtonItem(hasEmptyState))
        )

        return dashboardItems
    }

    private suspend fun getGreenCardItems(
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
            .map { greenCard ->
                if (greenCardUtil.isExpired(greenCard)) {
                    getExpiredBannerItem(
                        greenCard = greenCard
                    )
                } else {
                    mapGreenCardsItem(greenCard, isLoadingNewCredentials, databaseSyncerResult)
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

        // Always order by origin type
        items.sortBy {
            when (it) {
                is DashboardItem.CardsItem -> {
                    it.cards.first().originStates.first().origin.type.order
                }
                is DashboardItem.InfoItem.OriginInfoItem -> {
                    0
                }
                else -> {
                    0
                }
            }
        }

        return items
    }

    private fun getExpiredBannerItem(
        greenCard: GreenCard,
    ): DashboardItem {
        val origin = greenCard.origins.last()
        return when {
            greenCard.greenCardEntity.type is GreenCardType.Domestic && origin.type is OriginType.Vaccination -> {
                DashboardItem.InfoItem.DomesticVaccinationExpiredItem(greenCard.greenCardEntity)
            }
            greenCard.greenCardEntity.type is GreenCardType.Domestic && origin.type is OriginType.VaccinationAssessment -> {
                DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem(greenCard.greenCardEntity)
            }
            else -> {
                DashboardItem.InfoItem.GreenCardExpiredItem(
                    greenCardEntity = greenCard.greenCardEntity,
                    originType = origin.type
                )
            }
        }
    }

    private fun mapGreenCardsItem(
        greenCard: GreenCard,
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
            databaseSyncerResult = databaseSyncerResult
        )

        return DashboardItem.CardsItem(listOf(greenCardItem))
    }
}