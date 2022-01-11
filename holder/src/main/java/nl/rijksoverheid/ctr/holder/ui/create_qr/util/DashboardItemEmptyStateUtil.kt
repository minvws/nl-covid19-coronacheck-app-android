/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

interface DashboardItemEmptyStateUtil {
    fun hasEmptyState(hasVisitorPassIncompleteItem: Boolean,
                      allGreenCards: List<GreenCard>): Boolean
}

class DashboardItemEmptyStateUtilImpl(private val greenCardUtil: GreenCardUtil): DashboardItemEmptyStateUtil {

    override fun hasEmptyState(hasVisitorPassIncompleteItem: Boolean,
                               allGreenCards: List<GreenCard>): Boolean {

        val hasGreenCards = allGreenCards.isNotEmpty() && !allGreenCards.all { greenCardUtil.isExpired(it) }
        val domesticGreenCards = allGreenCards.filter { it.greenCardEntity.type == GreenCardType.Domestic }
        return !hasGreenCards && !hasVisitorPassIncompleteItem
    }
}