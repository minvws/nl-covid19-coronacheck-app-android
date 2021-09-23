package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItems
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*

interface GetDashboardItemsUseCase {
    suspend fun getItems(
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success,
        isLoadingNewCredentials: Boolean,
    ): DashboardItems
}

class GetDashboardItemsUseCaseImpl(
    private val greenCardUtil: GreenCardUtil,
    private val credentialUtil: CredentialUtil,
    private val originUtil: OriginUtil,
    private val dashboardItemUtil: DashboardItemUtil,
): GetDashboardItemsUseCase {
    override suspend fun getItems(
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
                isLoadingNewCredentials = isLoadingNewCredentials
            )
        )
    }

    private fun getDomesticItems(
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean,
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val domesticGreenCards = allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Domestic }
        val internationalGreenCards = allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Eu }

        if (dashboardItemUtil.shouldShowHeaderItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.HeaderItem(
                text = R.string.my_overview_description
            ))
        }

        if (dashboardItemUtil.shouldShowClockDeviationItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.ClockDeviationItem)
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCardType = GreenCardType.Domestic,
                greenCardsForSelectedType = domesticGreenCards,
                greenCardsForUnselectedType = internationalGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials
            )
        )

        if (dashboardItemUtil.shouldShowPlaceholderItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.PlaceholderCardItem(
                greenCardType = GreenCardType.Domestic
            ))
        }

        dashboardItems.add(
            DashboardItem.AddQrButtonItem(dashboardItemUtil.shouldAddQrButtonItem(allGreenCards))
        )

        return dashboardItems
    }

    private fun getInternationalItems(
        allGreenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        isLoadingNewCredentials: Boolean,
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val domesticGreenCards = allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Domestic }
        val internationalGreenCards = allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Eu }

        if (dashboardItemUtil.shouldShowHeaderItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.HeaderItem(
                text = R.string.my_overview_description_eu
            ))
        }

        if (dashboardItemUtil.shouldShowClockDeviationItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.ClockDeviationItem)
        }

        dashboardItems.addAll(
            getGreenCardItems(
                greenCardType = GreenCardType.Eu,
                greenCardsForSelectedType = internationalGreenCards,
                greenCardsForUnselectedType = domesticGreenCards,
                databaseSyncerResult = databaseSyncerResult,
                isLoadingNewCredentials = isLoadingNewCredentials
            )
        )

        if (dashboardItemUtil.shouldShowPlaceholderItem(allGreenCards)) {
            dashboardItems.add(DashboardItem.PlaceholderCardItem(
                greenCardType = GreenCardType.Eu
            ))
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
        isLoadingNewCredentials: Boolean
    ): List<DashboardItem> {

        // Loop through all green cards that exists in the database and map them to UI models
        val items = greenCardsForSelectedType.map { greenCard ->
            // If the origin with the highest possible expiration time is expired
            if (greenCardUtil.isExpired(greenCard)) {
                // Show green card expired banner
                DashboardItem.GreenCardExpiredItem(greenCard = greenCard)
            } else {
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
                    isLoadingNewCredentials -> DashboardItem.GreenCardItem.CredentialState.LoadingCredential
                    activeCredential == null -> DashboardItem.GreenCardItem.CredentialState.NoCredential
                    !hasValidOriginStates -> DashboardItem.GreenCardItem.CredentialState.NoCredential
                    else -> DashboardItem.GreenCardItem.CredentialState.HasCredential(activeCredential)
                }

                // Show green card
                DashboardItem.GreenCardItem(
                    greenCard = greenCard,
                    originStates = nonExpiredOriginStates,
                    credentialState = credentialState,
                    databaseSyncerResult = databaseSyncerResult
                )
            }
        }.toMutableList()

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
                is DashboardItem.GreenCardItem -> {
                    it.originStates.first().origin.type.order
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
}