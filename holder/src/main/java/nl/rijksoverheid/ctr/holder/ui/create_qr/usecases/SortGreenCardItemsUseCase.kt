/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface SortGreenCardItemsUseCase {
    fun sort(items: List<DashboardItem>): List<DashboardItem>
}

class SortGreenCardItemsUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
): SortGreenCardItemsUseCase {

    override fun sort(items: List<DashboardItem>): List<DashboardItem> {
        return items.sortedBy {
            when (it) {
                is DashboardItem.HeaderItem -> -100
                DashboardItem.InfoItem.ClockDeviationItem -> -99
                is DashboardItem.InfoItem.ConfigFreshnessWarning -> -98
                DashboardItem.InfoItem.AppUpdate -> -97
                DashboardItem.InfoItem.VisitorPassIncompleteItem -> -96
                is DashboardItem.InfoItem.GreenCardExpiredItem -> -95
                is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> -94
                is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> -93
                DashboardItem.InfoItem.BoosterItem -> -92
                DashboardItem.InfoItem.NewValidityItem -> -91
                is DashboardItem.InfoItem.DisclosurePolicyItem -> -90
                DashboardItem.InfoItem.MissingDutchVaccinationItem -> -89
                is DashboardItem.InfoItem.OriginInfoItem -> -88
                is DashboardItem.PlaceholderCardItem -> -87
                is DashboardItem.CardsItem -> {
                    val cardsItemOrder = -86
                    val greenCard = it.cards.first().greenCard
                    val isDomesticTestGreenCard = greenCardUtil.isDomesticTestGreenCard(
                        greenCard = greenCard
                    )
                    // If we are dealing with a domestic test green card and the policy is set to OneG, we always want that card on top
                    if (isDomesticTestGreenCard && featureFlagUseCase.getDisclosurePolicy() == DisclosurePolicy.OneG) {
                        cardsItemOrder
                    } else {
                        cardsItemOrder + it.cards.first().originStates.first().origin.type.order
                    }
                }
                DashboardItem.AddQrButtonItem -> -80
                DashboardItem.AddQrCardItem -> -79
                DashboardItem.CoronaMelderItem -> -78
            }
        }
    }
}