package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
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
    private val persistenceManager: PersistenceManager
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
                isLoadingNewCredentials = isLoadingNewCredentials
            ),
            internationalItems = getInternationalItems(
                allGreenCards = allGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                allEventGroupEntities = allEventGroupEntities
            )
        )
    }

    private fun getDomesticItems(
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean,
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val domesticGreenCards =
            allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Domestic }

        // Apply distinctBy here so that for two european green cards we do not get a two banners
        // saying "the certificate isn't valid in NL"
        val internationalGreenCards = allGreenCards
            .filter { it.greenCardEntity.type == GreenCardType.Eu }
            .distinctBy { it.greenCardEntity.type }

        val headerText = dashboardItemUtil.getHeaderItemText(
            greenCardType = GreenCardType.Domestic,
            allGreenCards = allGreenCards
        )

        dashboardItems.add(
            DashboardItem.HeaderItem(text = headerText)
        )

        if (dashboardItemUtil.shouldShowClockDeviationItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.ClockDeviationItem)
        }

        if (dashboardItemUtil.shouldShowExtendDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.NonDismissible.ExtendDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowRecoverDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.NonDismissible.RecoverDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowRecoveredDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.Dismissible.RecoveredDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowExtendedDomesticRecoveryItem()) {
            dashboardItems.add(DashboardItem.InfoItem.Dismissible.ExtendedDomesticRecovery)
        }

        if (dashboardItemUtil.shouldShowConfigFreshnessWarning()) {
            dashboardItems.add(
                DashboardItem.InfoItem.NonDismissible.ConfigFreshnessWarning(
                    maxValidityDate = dashboardItemUtil.getConfigFreshnessMaxValidity()
                )
            )
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCardType = GreenCardType.Domestic,
                greenCardsForSelectedType = domesticGreenCards,
                greenCardsForUnselectedType = internationalGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                combineVaccinations = false
            )
        )

        if (dashboardItemUtil.shouldShowPlaceholderItem(allGreenCards)) {
            dashboardItems.add(
                DashboardItem.PlaceholderCardItem(greenCardType = GreenCardType.Domestic)
            )
        }

        dashboardItems.add(
            DashboardItem.AddQrButtonItem(dashboardItemUtil.shouldAddQrButtonItem(allGreenCards))
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

        val headerText = dashboardItemUtil.getHeaderItemText(
            greenCardType = GreenCardType.Eu,
            allGreenCards = allGreenCards
        )

        dashboardItems.add(
            DashboardItem.HeaderItem(text = headerText)
        )

        if (dashboardItemUtil.shouldShowClockDeviationItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.ClockDeviationItem)
        }

        if (dashboardItemUtil.shouldAddSyncGreenCardsItem(allEventGroupEntities, allGreenCards)) {
            // Enable the ability to show GreenCardsSyncedItem (after successful sync)
            persistenceManager.setHasDismissedSyncedGreenCardsItem(false)
            dashboardItems.add(DashboardItem.InfoItem.NonDismissible.RefreshEuVaccinations)
        }

        if (dashboardItemUtil.shouldAddGreenCardsSyncedItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.InfoItem.Dismissible.RefreshedEuVaccinations)
        }

        if (dashboardItemUtil.shouldShowConfigFreshnessWarning()) {
            dashboardItems.add(
                DashboardItem.InfoItem.NonDismissible.ConfigFreshnessWarning(
                    maxValidityDate = dashboardItemUtil.getConfigFreshnessMaxValidity()
                )
            )
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCardType = GreenCardType.Eu,
                greenCardsForSelectedType = internationalGreenCards,
                greenCardsForUnselectedType = domesticGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials,
                combineVaccinations = true
            )
        )

        if (dashboardItemUtil.shouldShowPlaceholderItem(allGreenCards)) {
            dashboardItems.add(
                DashboardItem.PlaceholderCardItem(greenCardType = GreenCardType.Eu)
            )
        }

        dashboardItems.add(
            DashboardItem.AddQrButtonItem(dashboardItemUtil.shouldAddQrButtonItem(allGreenCards))
        )

        return dashboardItems
    }

    private fun getGreenCardItems(
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
                    DashboardItem.GreenCardExpiredItem(greenCard = greenCard)
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
                items.add(
                    DashboardItem.OriginInfoItem(
                        greenCardType = greenCardType,
                        originType = originForUnselectedType.type
                    )
                )
            }
        }

        // Always order by origin type
        items.sortBy {
            when (it) {
                is DashboardItem.CardsItem -> {
                    it.cards.first().originStates.first().origin.type.order
                }
                is DashboardItem.OriginInfoItem -> {
                    it.originType.order
                }
                else -> {
                    0
                }
            }
        }

        return items
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