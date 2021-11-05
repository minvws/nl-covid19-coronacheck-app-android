package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewInfoCardBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewInfoCardItem(
    private val infoItem: DashboardItem.InfoItem,
    private val onButtonClick: (infoItem: DashboardItem.InfoItem) -> Unit,
    private val onDismiss: (infoCardItem: MyOverviewInfoCardItem, infoItem: DashboardItem.InfoItem.Dismissible) -> Unit
) :
    BindableItem<ItemMyOverviewInfoCardBinding>(R.layout.item_my_overview_info_card.toLong()) {

    override fun bind(viewBinding: ItemMyOverviewInfoCardBinding, position: Int) {
        when (infoItem) {
            is DashboardItem.InfoItem.NonDismissible -> {
                // Non dismissible item does not have a close button
                viewBinding.close.visibility = View.GONE
            }
            is DashboardItem.InfoItem.Dismissible -> {
                // Non dismissible item has a close button with callback
                viewBinding.close.visibility = View.VISIBLE
                viewBinding.close.setOnClickListener {
                    onDismiss.invoke(this, infoItem)
                }
            }
        }

        when (infoItem) {
            is DashboardItem.InfoItem.NonDismissible.RefreshEuVaccinations -> {
                viewBinding.text.setText(R.string.dashboard_item_refresh_eu_vaccinations_text)
            }
            is DashboardItem.InfoItem.NonDismissible.ExtendDomesticRecovery -> {
                viewBinding.text.setText(R.string.extend_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.NonDismissible.RecoverDomesticRecovery -> {
                viewBinding.text.setText(R.string.recover_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.NonDismissible.ConfigFreshnessWarning -> {
                viewBinding.text.setText(R.string.config_warning_card_message)
            }
            is DashboardItem.InfoItem.Dismissible.ExtendedDomesticRecovery -> {
                viewBinding.text.setText(R.string.extended_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.Dismissible.RecoveredDomesticRecovery -> {
                viewBinding.text.setText(R.string.recovered_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.Dismissible.RefreshedEuVaccinations -> {
                viewBinding.text.setText(R.string.dashboard_item_refreshed_eu_vaccinations_text)
            }
        }

        viewBinding.button.setOnClickListener {
            onButtonClick.invoke(infoItem)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_info_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewInfoCardBinding {
        return ItemMyOverviewInfoCardBinding.bind(view)
    }
}
