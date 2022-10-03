/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface DashboardItemEmptyStateUtil {
    fun hasEmptyState(
        disclosurePolicy: DisclosurePolicy,
        hasVisitorPassIncompleteItem: Boolean,
        allGreenCards: List<GreenCard>,
        greenCardsForTab: List<GreenCard>
    ): Boolean
}

class DashboardItemEmptyStateUtilImpl(private val greenCardUtil: GreenCardUtil) :
    DashboardItemEmptyStateUtil {

    override fun hasEmptyState(
        disclosurePolicy: DisclosurePolicy,
        hasVisitorPassIncompleteItem: Boolean,
        allGreenCards: List<GreenCard>,
        greenCardsForTab: List<GreenCard>
    ): Boolean {

        return when (disclosurePolicy) {
            is DisclosurePolicy.ZeroG -> {
                greenCardsForTab.isEmpty()
            }
            else -> {
                val hasGreenCards = allGreenCards.isNotEmpty() && !allGreenCards.all { greenCardUtil.isExpired(it) }
                !hasGreenCards && !hasVisitorPassIncompleteItem
            }
        }
    }
}
