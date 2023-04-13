/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType

interface DashboardHeaderAdapterItemUtil {
    fun getHeaderItem(
        greenCardType: GreenCardType,
        emptyState: Boolean
    ): DashboardItem.HeaderItem
}

class DashboardHeaderAdapterItemUtilImpl : DashboardHeaderAdapterItemUtil {

    /**
     * Get the header item text to display in the domestic tab on the dashboard screen
     * @param greenCardType The type of the tab that is currently selected
     * @param emptyState If we should treat the dashboard as being empty state
     * @param hasVisitorPassIncompleteItem If there is a incomplete visitor pass item currently showing on the dashboard
     */
    override fun getHeaderItem(
        greenCardType: GreenCardType,
        emptyState: Boolean
    ): DashboardItem.HeaderItem {
        val text = getHeaderText(greenCardType, emptyState)
        val buttonInfo = getButtonInfo(greenCardType, emptyState)
        return DashboardItem.HeaderItem(text, buttonInfo)
    }

    private fun getHeaderText(
        tabType: GreenCardType,
        emptyState: Boolean
    ) = when (tabType) {
        is GreenCardType.Eu -> {
            if (emptyState) {
                R.string.holder_dashboard_emptyState_international_0G_message
            } else {
                R.string.holder_dashboard_filledState_international_0G_message
            }
        }
    }

    private fun getButtonInfo(tabType: GreenCardType, empty: Boolean) =
        if (tabType == GreenCardType.Eu) {
            if (empty) {
                ButtonInfo(
                    R.string.holder_dashboard_international_0G_action_certificateNeeded,
                    R.string.my_overview_description_eu_button_link
                )
            } else {
                ButtonInfo(
                    R.string.my_overview_description_eu_button_text,
                    R.string.my_overview_description_eu_button_link
                )
            }
        } else {
            null
        }
}
