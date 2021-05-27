package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import timber.log.Timber
import java.time.OffsetDateTime

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

class GetMyOverviewItemsUseCaseImpl(private val holderDatabase: HolderDatabase) :
    GetMyOverviewItemsUseCase {

    override suspend fun get(
        walletId: Int,
        selectedType: GreenCardType
    ): MyOverviewItems {
        return withContext(Dispatchers.IO) {
            val allGreenCards = holderDatabase.greenCardDao().getAll()
            val greenCardsForSelectedType =
                allGreenCards.filter { it.greenCardEntity.type == selectedType }

            val items = mutableListOf<MyOverviewItem>()
            items.add(
                getHeaderItem(
                    hasGreenCards = greenCardsForSelectedType.isNotEmpty(),
                    type = selectedType
                )
            )

            items.addAll(
                getGreenCardItems(
                    greenCards = greenCardsForSelectedType
                )
            )

            getCreateQrCardItem(
                hasGreenCards = items.any { it is MyOverviewItem.GreenCardItem },
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

        return MyOverviewItem.HeaderItem(
            text = text
        )
    }

    private suspend fun getGreenCardItems(greenCards: List<GreenCard>): List<MyOverviewItem> {
        if (greenCards.isEmpty()) {
            return listOf()
        } else {
            return greenCards.map { greenCard ->
                val orderedOrigins = greenCard.origins.sortedBy { it.expirationTime }

                // If the origin with the highest possible expiration time is expired
                if (OffsetDateTime.now() >= orderedOrigins.minByOrNull { it.expirationTime }!!.expirationTime) {
                    // Remove green card from database
                    holderDatabase.greenCardDao().delete(greenCard.greenCardEntity)

                    // Show green card expired banner
                    MyOverviewItem.GreenCardExpiredItem(greenCardType = greenCard.greenCardEntity.type)
                } else {
                    // Check if we have a credential
                    var activeCredential = greenCard.credentialEntities.firstOrNull {
                        it.validFrom.isBefore(
                        OffsetDateTime.now()) && it.expirationTime.isAfter(OffsetDateTime.now())
                    }

                    // Invalidate this credential if we only have one origin and that origin is not yet valid
                    if (greenCard.origins.size == 1 && greenCard.origins.first().validFrom.isAfter(OffsetDateTime.now())) {
                        activeCredential = null
                    }

                    // Show green card
                    MyOverviewItem.GreenCardItem(greenCard, orderedOrigins, activeCredential)
                }
            }
        }
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
                MyOverviewItem.CreateQrCardItem
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
                MyOverviewItem.TravelModeItem(R.string.travel_toggle_europe)
            }
            is GreenCardType.Domestic -> {
                val hasEuGreenCard = greenCards.any { it.greenCardEntity.type == GreenCardType.Eu }
                if (hasEuGreenCard) {
                    // Only return travel mode item if there are eu green cards
                    MyOverviewItem.TravelModeItem(R.string.travel_toggle_domestic)
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
        val sortedOrigins: List<OriginEntity>,
        val activeCredential: CredentialEntity?
    ) : MyOverviewItem()

    data class GreenCardExpiredItem(
        val greenCardType: GreenCardType
    ) : MyOverviewItem()

    data class TravelModeItem(@StringRes val text: Int) : MyOverviewItem()
}
