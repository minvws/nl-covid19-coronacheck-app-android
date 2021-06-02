package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.GreenCardItem.CredentialState
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem.GreenCardItem.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

    private suspend fun getGreenCardItems(greenCards: List<GreenCard>): List<MyOverviewItem> {
        if (greenCards.isEmpty()) {
            return listOf()
        } else {
            return greenCards.map { greenCard ->
                val orderedOrigins = greenCard.origins.sortedBy { it.expirationTime }

                // If the origin with the highest possible expiration time is expired
                if (OffsetDateTime.now(ZoneOffset.UTC) >= orderedOrigins.minByOrNull { it.expirationTime }!!.expirationTime) {
                    // Remove green card from database
                    holderDatabase.greenCardDao().delete(greenCard.greenCardEntity)

                    // Show green card expired banner
                    GreenCardExpiredItem(greenCardType = greenCard.greenCardEntity.type)
                } else {
                    // Check if we have a credential
                    val activeCredential = credentialUtil.getActiveCredential(
                        entities = greenCard.credentialEntities
                    )

                    // Check if all of our origins are valid (are in between the current time window)
                    val validOrigins = originUtil.getValidOrigins(
                        origins = greenCard.origins
                    )

                    // Map our origin to more readable states
                    val originStates = orderedOrigins.map { origin ->
                        if (validOrigins.contains(origin)) OriginState.ValidOrigin(origin) else OriginState.InvalidOrigin(origin)
                    }

                    var cardActive = CardActive()
                    if (greenCard.greenCardEntity.type == GreenCardType.Eu) {
                        val euLaunchDate = cachedAppConfigUseCase.getCachedAppConfig()!!.euLaunchDate
                        cardActive = CardActive(
                            isActive = originUtil.isActiveInEu(euLaunchDate),
                            activeInDays = originUtil.daysSinceActive(euLaunchDate),
                        )
                    }

                    // More our credential to a more readable state
                    val credentialState = when {
                        activeCredential == null -> CredentialState.NoCredential
                        validOrigins.isEmpty() -> CredentialState.NoCredential
                        !cardActive.isActive -> CredentialState.NoCredential
                        else -> CredentialState.HasCredential(activeCredential)
                    }

                    // Show green card
                    GreenCardItem(
                        greenCard = greenCard,
                        originStates = if (!cardActive.isActive) {
                            listOf(OriginState.InvalidOrigin(originStates.first().origin))
                        } else {
                            originStates
                        },
                        credentialState = credentialState,
                        active = cardActive,
                    )
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
                val hasEuGreenCard = greenCards.any { it.greenCardEntity.type == GreenCardType.Eu }
                if (hasEuGreenCard) {
                    // Only return travel mode item if there are eu green cards
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

    data class CardActive(val isActive: Boolean = true, val activeInDays: Long = 0)

    data class GreenCardItem(
        val greenCard: GreenCard,
        val originStates: List<OriginState>,
        val credentialState: CredentialState,
        val active: CardActive,
    ) : MyOverviewItem() {

        sealed class OriginState(open val origin: OriginEntity) {
            data class ValidOrigin(override val origin: OriginEntity): OriginState(origin)
            data class InvalidOrigin(override val origin: OriginEntity): OriginState(origin)
        }

        sealed class CredentialState {
            data class HasCredential(val credential: CredentialEntity): CredentialState()
            object NoCredential : CredentialState()
        }
    }

    data class GreenCardExpiredItem(
        val greenCardType: GreenCardType
    ) : MyOverviewItem()

    data class TravelModeItem(@StringRes val text: Int) : MyOverviewItem()
}
