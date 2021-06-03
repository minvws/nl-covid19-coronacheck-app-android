package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.GreenCardItem.CredentialState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
        selectedType: GreenCardType
    ): MyOverviewItems
}

class GetMyOverviewItemsUseCaseImpl(private val holderDatabase: HolderDatabase,
                                    private val credentialUtil: CredentialUtil,
                                    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
                                    private val originUtil: OriginUtil) :
    GetMyOverviewItemsUseCase {

    override suspend fun get(
        walletId: Int,
        selectedType: GreenCardType
    ): MyOverviewItems {
        return withContext(Dispatchers.IO) {
            val unselectedType = when (selectedType) {
                is GreenCardType.Domestic -> GreenCardType.Eu
                is GreenCardType.Eu -> GreenCardType.Domestic
            }

            val allGreenCards = holderDatabase.greenCardDao().getAll()
            val greenCardsForSelectedType = allGreenCards.filter { it.greenCardEntity.type == selectedType }
            val greenCardsForUnselectedType = allGreenCards.filter { it.greenCardEntity.type == unselectedType }

            val items = mutableListOf<MyOverviewItem>()
            items.add(
                getHeaderItem(
                    hasGreenCards = greenCardsForSelectedType.isNotEmpty(),
                    type = selectedType
                )
            )

            items.addAll(
                getGreenCardItems(
                    selectedType = selectedType,
                    greenCardsForSelectedType = greenCardsForSelectedType,
                    greenCardsForUnselectedType = greenCardsForUnselectedType
                )
            )

            getCreateQrCardItem(
                hasGreenCards = items.any { it is GreenCardItem },
                selectedType = selectedType,
            )?.let {
                items.add(it)
            }

            getTravelModeItem(
                greenCards = allGreenCards,
                selectedType = selectedType
            )?.let {
                items.add(it)
            }

            MyOverviewItems(
                items = items,
                selectedType = selectedType
            )
        }
    }

    private fun getHeaderItem(hasGreenCards: Boolean, type: GreenCardType): MyOverviewItem {
        // Text for header depends on some factors
        val text = when (type) {
            is GreenCardType.Domestic -> {
                if (hasGreenCards) {
                    R.string.my_overview_description
                } else {
                    R.string.my_overview_no_qr_description
                }
            }
            is GreenCardType.Eu -> {
                R.string.my_overview_description_eu
            }
        }

        return HeaderItem(
            text = text
        )
    }

    private suspend fun getGreenCardItems(selectedType: GreenCardType,
                                          greenCardsForSelectedType: List<GreenCard>,
                                          greenCardsForUnselectedType: List<GreenCard>): List<MyOverviewItem> {

        // Loop through all green cards that exists in the database and map them to UI models
        val items = greenCardsForSelectedType.map { greenCard ->
            // If the origin with the highest possible expiration time is expired
            if (OffsetDateTime.now(ZoneOffset.UTC) >= greenCard.origins.maxByOrNull { it.expirationTime }!!.expirationTime) {
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
                )

                // Filter out origins that are expired
                val filteredOriginStates = originStates.filter { it !is OriginState.Expired }

                val euL = "2021-06-03T14:00:00+00:00"

                val euLaunchDate = OffsetDateTime.parse(euL, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                val launchDate = if (greenCard.greenCardEntity.type == GreenCardType.Eu) euLaunchDate else OffsetDateTime.now()

                // More our credential to a more readable state
                val credentialState = when {
                    activeCredential == null -> CredentialState.NoCredential
                    filteredOriginStates.isEmpty() -> CredentialState.NoCredential
                    launchDate.isAfter(OffsetDateTime.now()) -> CredentialState.NoCredential
                    else -> CredentialState.HasCredential(activeCredential)
                }

                // Show green card
                GreenCardItem(
                    greenCard = greenCard,
                    originStates = filteredOriginStates,
                    credentialState = credentialState,
                    launchDate = launchDate,
                )
            }
        }.toMutableList()

        // If we have valid origins that exists in the other selected type but not in the current one, we show a banner
        val allOriginsForSelectedType = greenCardsForSelectedType.map { it.origins }.flatten()
        val allOriginsForUnselectedType = greenCardsForUnselectedType.map { it.origins }.flatten()
        val allValidOriginsForSelectedType = originUtil.getOriginState(allOriginsForSelectedType).filterIsInstance<OriginState.Valid>().map { it.origin }
        val allValidOriginsForUnselectedType = originUtil.getOriginState(allOriginsForUnselectedType).filterIsInstance<OriginState.Valid>().map { it.origin }

        allValidOriginsForUnselectedType.forEach { originForUnselectedType ->
            if (!allValidOriginsForSelectedType.map { it.type }.contains(originForUnselectedType.type)) {
                items.add(
                    OriginInfoItem(
                        greenCardType = selectedType,
                        originType = originForUnselectedType.type
                    )
                )
            }
        }

        return items
    }

    private fun getCreateQrCardItem(
        hasGreenCards: Boolean,
        selectedType: GreenCardType
    ): MyOverviewItem? {
        return if (hasGreenCards) {
            null
        } else {
            // Only return create qr card if there are not green cards on the screen and we have domestic type selected
            if (selectedType == GreenCardType.Domestic) {
                CreateQrCardItem
            } else {
                null
            }
        }
    }

    private fun getTravelModeItem(
        greenCards: List<GreenCard>,
        selectedType: GreenCardType
    ): MyOverviewItem? {
        return when (selectedType) {
            is GreenCardType.Eu -> {
                TravelModeItem(R.string.travel_toggle_europe)
            }
            is GreenCardType.Domestic -> {
                if (greenCards.isNotEmpty()) {
                    TravelModeItem(R.string.travel_toggle_domestic)
                } else {
                    null
                }
            }
        }
    }
}

data class MyOverviewItems(
    val items: List<MyOverviewItem>,
    val selectedType: GreenCardType
)

sealed class MyOverviewItem {

    data class HeaderItem(@StringRes val text: Int) : MyOverviewItem()

    object CreateQrCardItem : MyOverviewItem()

    data class GreenCardItem(
        val greenCard: GreenCard,
        val originStates: List<OriginState>,
        val credentialState: CredentialState,
        val launchDate: OffsetDateTime
    ) : MyOverviewItem() {

        sealed class CredentialState {
            data class HasCredential(val credential: CredentialEntity): CredentialState()
            object NoCredential : CredentialState()
        }
    }

    data class GreenCardExpiredItem(
        val greenCardType: GreenCardType
    ) : MyOverviewItem()

    data class TravelModeItem(@StringRes val text: Int) : MyOverviewItem()
    data class OriginInfoItem(val greenCardType: GreenCardType, val originType: OriginType): MyOverviewItem()
}
