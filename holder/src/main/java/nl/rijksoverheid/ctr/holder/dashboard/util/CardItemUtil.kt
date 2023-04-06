/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
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

    @StringRes
    fun getQrCodesFragmentToolbarTitle(cardItem: DashboardItem.CardsItem.CardItem): Int
}

class CardItemUtilImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
) : CardItemUtil {

    override fun getDisclosurePolicy(
        greenCard: GreenCard,
        greenCardIndex: Int
    ): GreenCardDisclosurePolicy {
            return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                GreenCardDisclosurePolicy.ThreeG
            }
        }
    }

    override fun getEnabledState(
        greenCard: GreenCard
    ): GreenCardEnabledState {
        return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                GreenCardEnabledState.Enabled
            }
        }
    }

    override fun shouldDisclose(cardItem: DashboardItem.CardsItem.CardItem): QrCodeFragmentData.ShouldDisclose {
        return when (cardItem.greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                QrCodeFragmentData.ShouldDisclose.DoNotDisclose
            }
        }
    }

    override fun getQrCodesFragmentToolbarTitle(cardItem: DashboardItem.CardsItem.CardItem): Int {
        return when (featureFlagUseCase.getDisclosurePolicy()) {
            DisclosurePolicy.OneAndThreeG -> {
                getTitleFromCardDisclosurePolicy(cardItem.disclosurePolicy)
            }
            DisclosurePolicy.OneG -> getTitleFromCardDisclosurePolicy(cardItem.disclosurePolicy)
            else -> R.string.domestic_qr_code_title
        }
    }

    private fun getTitleFromCardDisclosurePolicy(policy: GreenCardDisclosurePolicy) = when (policy) {
        GreenCardDisclosurePolicy.OneG -> R.string.holder_showQR_domestic_title_1g
        GreenCardDisclosurePolicy.ThreeG -> R.string.holder_showQR_domestic_title_3g
    }
}
