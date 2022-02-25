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
        return items.sortedByDescending {
            when (it) {
                is DashboardItem.HeaderItem -> 200
                DashboardItem.InfoItem.ClockDeviationItem -> 190
                is DashboardItem.InfoItem.ConfigFreshnessWarning -> 180
                DashboardItem.InfoItem.AppUpdate -> 170
                DashboardItem.InfoItem.VisitorPassIncompleteItem -> 160
                is DashboardItem.InfoItem.GreenCardExpiredItem -> 150
                is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> 140
                is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> 130
                DashboardItem.InfoItem.BoosterItem -> 120
                DashboardItem.InfoItem.NewValidityItem -> 110
                is DashboardItem.InfoItem.DisclosurePolicyItem -> 100
                DashboardItem.InfoItem.MissingDutchVaccinationItem -> 90
                is DashboardItem.InfoItem.OriginInfoItem -> 80
                is DashboardItem.PlaceholderCardItem -> 70
                is DashboardItem.CardsItem -> {
                    val cardsItemOrder = 60
                    val greenCard = it.cards.first().greenCard
                    val isDomesticTestGreenCard = greenCardUtil.isDomesticTestGreenCard(
                        greenCard = greenCard
                    )
                    // If we are dealing with a domestic test green card and the policy is set to OneG, we always want that card on top
                    if (isDomesticTestGreenCard && featureFlagUseCase.getDisclosurePolicy() == DisclosurePolicy.OneG) {
                        cardsItemOrder
                    } else {
                        cardsItemOrder - it.cards.first().originStates.first().origin.type.order
                    }
                }
                DashboardItem.AddQrButtonItem -> 50
                DashboardItem.AddQrCardItem -> 40
                DashboardItem.CoronaMelderItem -> 30
            }
        }
    }
}