/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.usecases

import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface SortGreenCardItemsUseCase {
    fun sort(items: List<DashboardItem>): List<DashboardItem>
}

class SortGreenCardItemsUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
): SortGreenCardItemsUseCase {

    override fun sort(items: List<DashboardItem>): List<DashboardItem> {
        if (items.size < 2) {
            return items
        }
        return items.sortedBy {
            when (it) {
                is DashboardItem.HeaderItem -> 10
                DashboardItem.InfoItem.ClockDeviationItem -> 20
                is DashboardItem.InfoItem.ConfigFreshnessWarning -> 30
                DashboardItem.InfoItem.AppUpdate -> 40
                DashboardItem.InfoItem.VisitorPassIncompleteItem -> 50
                is DashboardItem.InfoItem.GreenCardExpiredItem -> 60
                is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> 70
                is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> 80
                is DashboardItem.InfoItem.DisclosurePolicyItem -> 90
                DashboardItem.InfoItem.MissingDutchVaccinationItem -> 100
                is DashboardItem.InfoItem.OriginInfoItem -> 110
                is DashboardItem.PlaceholderCardItem -> 120
                is DashboardItem.CardsItem -> {
                    val cardsItemOrder = 130
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
                DashboardItem.AddQrButtonItem -> 140
                DashboardItem.AddQrCardItem -> 150
            }
        }
    }
}