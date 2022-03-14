/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface DashboardHeaderAdapterItemUtil {
    fun getText(
        greenCardType: GreenCardType,
        emptyState: Boolean,
        hasVisitorPassIncompleteItem: Boolean
        ): Int
}

class DashboardHeaderAdapterItemUtilImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase
): DashboardHeaderAdapterItemUtil {

    /**
     * Get the header item text to display in the domestic tab on the dashboard screen
     * @param tabType The type of the tab that is currently selected
     * @param emptyState If we should treat the dashboard as being empty state
     * @param hasVisitorPassIncompleteItem If there is a incomplete visitor pass item currently showing on the dashboard
     */
    override fun getText(
        tabType: GreenCardType,
        emptyState: Boolean,
        hasVisitorPassIncompleteItem: Boolean): Int {

        when (tabType) {
            is GreenCardType.Domestic -> {
                val emptyState = emptyState || hasVisitorPassIncompleteItem

                return when (featureFlagUseCase.getDisclosurePolicy()) {
                    is DisclosurePolicy.OneG -> {
                        if (emptyState) {
                            R.string.holder_dashboard_empty_domestic_only1Gaccess_message
                        } else {
                            R.string.holder_dashboard_intro_domestic_only1Gaccess
                        }
                    }
                    is DisclosurePolicy.OneAndThreeG -> {
                        if (emptyState) {
                            R.string.holder_dashboard_empty_domestic_3Gand1Gaccess_message
                        } else {
                            R.string.holder_dashboard_intro_domestic_3Gand1Gaccess
                        }
                    }
                    is DisclosurePolicy.ThreeG -> {
                        if (emptyState) {
                            R.string.my_overview_qr_placeholder_description
                        } else {
                            R.string.my_overview_description
                        }
                    }
                }
            }
            is GreenCardType.Eu -> {
                return if (emptyState) {
                    R.string.my_overview_qr_placeholder_description_eu
                } else {
                    R.string.my_overview_description_eu
                }
            }
        }
    }
}