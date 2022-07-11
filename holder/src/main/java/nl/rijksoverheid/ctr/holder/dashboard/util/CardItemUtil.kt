/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy

interface CardItemUtil {
    fun getDisclosurePolicy(
        greenCard: GreenCard,
        greenCardIndex: Int
    ): GreenCardDisclosurePolicy

    fun getEnabledState(
        greenCard: GreenCard
    ): GreenCardEnabledState

    fun shouldDisclose(
        cardItem: DashboardItem.CardsItem.CardItem
    ): QrCodeFragmentData.ShouldDisclose
}

class CardItemUtilImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
): CardItemUtil {

    override fun getDisclosurePolicy(
        greenCard: GreenCard,
        greenCardIndex: Int
        ): GreenCardDisclosurePolicy {
            return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Domestic -> {
                val isGreenCardWithOnlyTestOrigin = greenCard.origins.all { it.type is OriginType.Test }

                when (featureFlagUseCase.getDisclosurePolicy()) {
                    is DisclosurePolicy.OneG -> {
                        if (isGreenCardWithOnlyTestOrigin) {
                            GreenCardDisclosurePolicy.OneG
                        } else {
                            GreenCardDisclosurePolicy.ThreeG
                        }
                    }
                    is DisclosurePolicy.ThreeG -> {
                        GreenCardDisclosurePolicy.ThreeG
                    }
                    is DisclosurePolicy.OneAndThreeG -> {
                        if (isGreenCardWithOnlyTestOrigin) {
                            if (greenCardIndex == 0) {
                                GreenCardDisclosurePolicy.ThreeG
                            } else {
                                GreenCardDisclosurePolicy.OneG
                            }
                        } else {
                            GreenCardDisclosurePolicy.ThreeG
                        }
                    }
                    DisclosurePolicy.ZeroG -> GreenCardDisclosurePolicy.ThreeG // Not relevant for domestic card
                }
            }
            is GreenCardType.Eu -> {
                GreenCardDisclosurePolicy.ThreeG
            }
        }
    }

    override fun getEnabledState(
        greenCard: GreenCard
    ): GreenCardEnabledState {
        return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Domestic -> {
                if (featureFlagUseCase.getDisclosurePolicy() == DisclosurePolicy.OneG &&
                    !greenCardUtil.hasOrigin(listOf(greenCard), OriginType.Test) ) {
                    GreenCardEnabledState.Disabled()
                } else {
                    GreenCardEnabledState.Enabled
                }
            }
            is GreenCardType.Eu -> {
                GreenCardEnabledState.Enabled
            }
        }
    }

    override fun shouldDisclose(cardItem: DashboardItem.CardsItem.CardItem): QrCodeFragmentData.ShouldDisclose {
        return when (cardItem.greenCard.greenCardEntity.type) {
            is GreenCardType.Domestic -> {
                QrCodeFragmentData.ShouldDisclose.Disclose(
                    cardItem.greenCard.greenCardEntity.id,
                    cardItem.disclosurePolicy
                )
            }
            is GreenCardType.Eu -> {
                QrCodeFragmentData.ShouldDisclose.DoNotDisclose
            }
        }
    }
}