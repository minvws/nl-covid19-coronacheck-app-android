package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.GreenCardItem.CredentialState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Get all cards that should be displayed in MyOverviewFragment
 */
interface GetMyOverviewItemsUseCase {
    suspend fun get(
        walletId: Int,
        selectedType: GreenCardType,
        databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success,
        shouldRefresh: Boolean,
        hasClockDeviation: Boolean
    ): MyOverviewItems

    suspend fun getGreenCards(): List<GreenCard>
}

class GetMyOverviewItemsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val credentialUtil: CredentialUtil,
    private val greenCardUtil: GreenCardUtil,
    private val originUtil: OriginUtil
) : GetMyOverviewItemsUseCase {

    override suspend fun get(
        walletId: Int,
        selectedType: GreenCardType,
        databaseSyncerResult: DatabaseSyncerResult,
        shouldRefresh: Boolean,
        hasClockDeviation: Boolean
    ): MyOverviewItems {
        return withContext(Dispatchers.IO) {
            val unselectedType = when (selectedType) {
                is GreenCardType.Domestic -> GreenCardType.Eu
                is GreenCardType.Eu -> GreenCardType.Domestic
            }

            val allGreenCards = holderDatabase.greenCardDao().getAll()
            val greenCardsForSelectedType =
                allGreenCards.filter { it.greenCardEntity.type == selectedType }
            val greenCardsForUnselectedType =
                allGreenCards.filter { it.greenCardEntity.type == unselectedType }

            val items = mutableListOf<MyOverviewItem>()

            getHeaderItem(allGreenCards, selectedType)?.let {
                items.add(
                    it
                ).also {
                    if(hasClockDeviation) {
                        // Add below header but before greencards
                        getClockDeviationItem(allGreenCards, selectedType)?.let { item ->
                            items.add(
                                index = 1,
                                item
                            )
                        }
                    }
                }
            }

            items.addAll(
                getGreenCardItems(
                    selectedType = selectedType,
                    greenCardsForSelectedType = greenCardsForSelectedType,
                    greenCardsForUnselectedType = greenCardsForUnselectedType,
                    databaseSyncerResult = databaseSyncerResult,
                    shouldRefresh = shouldRefresh
                )
            )



            getCreatePlaceholderCardItem(allGreenCards, selectedType)?.let {
                items.add(it)
            }

            MyOverviewItems(
                items = items,
                selectedType = selectedType
            )
        }
    }

    private fun getHeaderItem(greenCards: List<GreenCard>,
                              type: GreenCardType): MyOverviewItem? {
        return if (greenCards.isEmpty() || greenCards.all { greenCardUtil.isExpired(it) }) {
            null
        } else {
            val text = when (type) {
                is GreenCardType.Domestic -> R.string.my_overview_description
                is GreenCardType.Eu -> R.string.my_overview_description_eu
            }

            HeaderItem(
                text = text
            )
        }
    }

    private fun getClockDeviationItem(greenCards: List<GreenCard>,
                                      type: GreenCardType) : MyOverviewItem?{
        return if (greenCards.isEmpty() || greenCards.all { greenCardUtil.isExpired(it) }) {
            null
        } else {
            ClockDeviationItem
        }
    }

    private suspend fun getGreenCardItems(
        selectedType: GreenCardType,
        greenCardsForSelectedType: List<GreenCard>,
        greenCardsForUnselectedType: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult,
        shouldRefresh: Boolean
    ): List<MyOverviewItem> {

        // Loop through all green cards that exists in the database and map them to UI models
        val items = greenCardsForSelectedType.map { greenCard ->
            // If the origin with the highest possible expiration time is expired
            if (greenCardUtil.isExpired(greenCard)) {
                // Remove green card from database
                holderDatabase.greenCardDao().delete(greenCard.greenCardEntity)

                // Show green card expired banner
                GreenCardExpiredItem(greenCardType = greenCard.greenCardEntity.type)
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
                    shouldRefresh -> CredentialState.LoadingCredential
                    activeCredential == null -> CredentialState.NoCredential
                    !hasValidOriginStates -> CredentialState.NoCredential
                    else -> CredentialState.HasCredential(activeCredential)
                }

                // Show green card
                GreenCardItem(
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
                    OriginInfoItem(
                        greenCardType = selectedType,
                        originType = originForUnselectedType.type
                    )
                )
            }
        }

        // Always order by origin type
        items.sortBy {
            when (it) {
                is GreenCardItem -> {
                    it.originStates.first().origin.type.order
                }
                is OriginInfoItem -> {
                    it.originType.order
                }
                else -> {
                    0
                }
            }
        }

        return items
    }

    private fun getCreatePlaceholderCardItem(
        greenCards: List<GreenCard>,
        greenCardType: GreenCardType
    ): MyOverviewItem? {
        return if (greenCards.isEmpty() || greenCards.all { greenCardUtil.isExpired(it) }) {
            PlaceholderCardItem(greenCardType)
        } else {
            null
        }
    }

    override suspend fun getGreenCards(): List<GreenCard> {
        return holderDatabase.greenCardDao().getAll()
    }
}

data class MyOverviewItems(
    val items: List<MyOverviewItem>,
    val selectedType: GreenCardType,
)

sealed class MyOverviewItem {

    data class HeaderItem(@StringRes val text: Int) : MyOverviewItem()
    data class PlaceholderCardItem(val greenCardType: GreenCardType) : MyOverviewItem()
    object ClockDeviationItem : MyOverviewItem()

    data class GreenCardItem(
        val greenCard: GreenCard,
        val originStates: List<OriginState>,
        val credentialState: CredentialState,
        val databaseSyncerResult: DatabaseSyncerResult
    ) : MyOverviewItem() {

        sealed class CredentialState {
            data class HasCredential(val credential: CredentialEntity) : CredentialState()
            object LoadingCredential: CredentialState()
            object NoCredential : CredentialState()
        }
    }

    data class GreenCardExpiredItem(val greenCardType: GreenCardType) : MyOverviewItem()
    data class OriginInfoItem(val greenCardType: GreenCardType, val originType: OriginType) : MyOverviewItem()
}
