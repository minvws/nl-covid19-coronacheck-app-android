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
                is DashboardItem.HeaderItem -> 10
                DashboardItem.InfoItem.ClockDeviationItem -> 20
                is DashboardItem.InfoItem.ConfigFreshnessWarning -> 30
                DashboardItem.InfoItem.AppUpdate -> 40
                DashboardItem.InfoItem.VisitorPassIncompleteItem -> 50
                is DashboardItem.InfoItem.GreenCardExpiredItem -> 60
                is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> 70
                is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> 80
                DashboardItem.InfoItem.BoosterItem -> 90
                DashboardItem.InfoItem.NewValidityItem -> 100
                is DashboardItem.InfoItem.DisclosurePolicyItem -> 110
                DashboardItem.InfoItem.MissingDutchVaccinationItem -> 120
                is DashboardItem.InfoItem.OriginInfoItem -> 130
                is DashboardItem.PlaceholderCardItem -> 140
                is DashboardItem.CardsItem -> {
                    val cardsItemOrder = 150
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
                DashboardItem.AddQrButtonItem -> 160
                DashboardItem.AddQrCardItem -> 170
                DashboardItem.CoronaMelderItem -> 180
            }
        }
    }
}