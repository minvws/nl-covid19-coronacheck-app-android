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

interface SortGreenCardItemsUseCase {
    fun sort(items: List<DashboardItem>): List<DashboardItem>
}

class SortGreenCardItemsUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
) : SortGreenCardItemsUseCase {

    override fun sort(items: List<DashboardItem>): List<DashboardItem> {
        if (items.size < 2) {
            return items
        }
        return items.sortedBy {
            when (it) {
                is DashboardItem.HeaderItem -> 10
                DashboardItem.InfoItem.ClockDeviationItem -> 20
                is DashboardItem.InfoItem.ConfigFreshnessWarning -> 30
                is DashboardItem.InfoItem.BlockedEvents -> 40
                is DashboardItem.InfoItem.FuzzyMatchedEvents -> 45
                DashboardItem.InfoItem.AppUpdate -> 50
                is DashboardItem.InfoItem.GreenCardExpiredItem -> 70
                is DashboardItem.InfoItem.OriginInfoItem -> 120
                is DashboardItem.PlaceholderCardItem -> 130
                is DashboardItem.CardsItem -> {
                    val cardsItemOrder = 140
                    cardsItemOrder + (it.cards.firstOrNull()?.originStates?.firstOrNull()?.origin?.type?.order ?: 1)
                }
                DashboardItem.AddQrButtonItem -> 150
                DashboardItem.AddQrCardItem -> 160
            }
        }
    }
}
