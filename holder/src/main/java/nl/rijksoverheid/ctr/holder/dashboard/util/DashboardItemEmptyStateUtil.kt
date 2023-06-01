/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import nl.rijksoverheid.ctr.persistence.database.models.GreenCard

interface DashboardItemEmptyStateUtil {
    fun hasEmptyState(
        allGreenCards: List<GreenCard>,
        greenCardsForTab: List<GreenCard>
    ): Boolean
}

class DashboardItemEmptyStateUtilImpl :
    DashboardItemEmptyStateUtil {

    override fun hasEmptyState(
        allGreenCards: List<GreenCard>,
        greenCardsForTab: List<GreenCard>
    ): Boolean {

        return greenCardsForTab.isEmpty()
    }
}
