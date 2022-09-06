/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface DashboardHeaderAdapterItemUtil {
    fun getHeaderItem(
        greenCardType: GreenCardType,
        emptyState: Boolean,
        hasVisitorPassIncompleteItem: Boolean
    ): DashboardItem.HeaderItem
}

class DashboardHeaderAdapterItemUtilImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase
) : DashboardHeaderAdapterItemUtil {

    /**
     * Get the header item text to display in the domestic tab on the dashboard screen
     * @param tabType The type of the tab that is currently selected
     * @param emptyState If we should treat the dashboard as being empty state
     * @param hasVisitorPassIncompleteItem If there is a incomplete visitor pass item currently showing on the dashboard
     */
    override fun getHeaderItem(
        tabType: GreenCardType,
        emptyState: Boolean,
        hasVisitorPassIncompleteItem: Boolean
    ): DashboardItem.HeaderItem {
        val empty = emptyState || hasVisitorPassIncompleteItem
        val text = if (hasVisitorPassIncompleteItem) {
            R.string.holder_dashboard_incompleteVisitorPass_message
        } else {
            getHeaderText(tabType, empty)
        }
        val buttonInfo = getButtonInfo(tabType, empty)
        return DashboardItem.HeaderItem(text, buttonInfo)
    }

    private fun getHeaderText(
        tabType: GreenCardType,
        emptyState: Boolean
    ) = when (tabType) {
        is GreenCardType.Domestic -> {
            when (featureFlagUseCase.getDisclosurePolicy()) {
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
                is DisclosurePolicy.ZeroG -> R.string.app_name // Not applicable
            }
        }
        is GreenCardType.Eu -> {
            if (emptyState) {
                when (featureFlagUseCase.getDisclosurePolicy()) {
                    is DisclosurePolicy.ZeroG -> {
                        R.string.holder_dashboard_emptyState_international_0G_message
                    }
                    else -> {
                        R.string.my_overview_qr_placeholder_description_eu
                    }
                }
            } else {
                when (featureFlagUseCase.getDisclosurePolicy()) {
                    is DisclosurePolicy.ZeroG -> {
                        R.string.holder_dashboard_filledState_international_0G_message
                    }
                    else -> {
                        R.string.my_overview_description_eu
                    }
                }
            }
        }
    }

    private fun getButtonInfo(tabType: GreenCardType, empty: Boolean) =
        if (tabType == GreenCardType.Eu) {
            if (featureFlagUseCase.getDisclosurePolicy() is DisclosurePolicy.ZeroG && empty) {
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
