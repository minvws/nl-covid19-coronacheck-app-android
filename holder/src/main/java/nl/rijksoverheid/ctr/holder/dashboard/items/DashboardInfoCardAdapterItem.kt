/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemDashboardInfoCardBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DashboardInfoCardAdapterItem(
    private val infoItem: DashboardItem.InfoItem,
    private val onButtonClick: (infoItem: DashboardItem.InfoItem) -> Unit,
    private val onDismiss: (infoCardAdapterItem: DashboardInfoCardAdapterItem, infoItem: DashboardItem.InfoItem) -> Unit = { _, _ -> }
) : BindableItem<AdapterItemDashboardInfoCardBinding>(R.layout.adapter_item_dashboard_info_card.toLong()),
    KoinComponent {

    private val utilAdapter: DashboardInfoCardAdapterItemUtil by inject()

    override fun bind(viewBinding: AdapterItemDashboardInfoCardBinding, position: Int) {
        if (infoItem.isDismissible) {
            // dismissible item has a close button with callback
            viewBinding.close.visibility = View.VISIBLE
            viewBinding.close.setOnClickListener {
                onDismiss.invoke(this, infoItem)
            }
        } else {
            // Non dismissible item does not have a close button
            viewBinding.close.visibility = View.GONE
        }

        viewBinding.button.run {
            val buttonTextId = infoItem.buttonText ?: R.string.general_readmore
            visibility = if (infoItem.hasButton) View.VISIBLE else View.GONE
            setText(buttonTextId)
            contentDescription = context.getString(buttonTextId)
        }

        when (infoItem) {
            is DashboardItem.InfoItem.ConfigFreshnessWarning -> {
                viewBinding.text.setText(R.string.config_warning_card_message)
            }
            is DashboardItem.InfoItem.ClockDeviationItem -> {
                viewBinding.text.setText(R.string.my_overview_clock_deviation_description)
            }
            is DashboardItem.InfoItem.GreenCardExpiredItem -> {
                val expiredItemText = utilAdapter.getExpiredItemText(
                    greenCardType = infoItem.greenCardType,
                    originType = infoItem.originEntity.type
                )
                viewBinding.text.text = viewBinding.root.context.getString(expiredItemText)
            }
            is DashboardItem.InfoItem.OriginInfoItem -> {
                viewBinding.text.text =
                    utilAdapter.getOriginInfoText(infoItem, viewBinding.dashboardItemInfoRoot.context)
            }
            is DashboardItem.InfoItem.MissingDutchVaccinationItem -> {
                viewBinding.text.text =
                    viewBinding.text.context.getString(R.string.missing_dutch_certificate_info_card_text)
            }
            is DashboardItem.InfoItem.AppUpdate -> {
                viewBinding.text.setText(R.string.recommended_update_card_description)
            }
            is DashboardItem.InfoItem.VisitorPassIncompleteItem -> {
                viewBinding.text.setText(R.string.holder_dashboard_visitorpassincompletebanner_title)
            }
            is DashboardItem.InfoItem.BlockedEvents -> {
                viewBinding.text.setText(
                    R.string.holder_invaliddetailsremoved_banner_title
                )
            }
            is DashboardItem.InfoItem.FuzzyMatchedEvents -> {
                viewBinding.text.setText(
                    R.string.holder_identityRemoved_banner_title
                )
            }
        }

        viewBinding.button.setOnClickListener {
            onButtonClick.invoke(infoItem)
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_dashboard_info_card
    }

    override fun initializeViewBinding(view: View): AdapterItemDashboardInfoCardBinding {
        return AdapterItemDashboardInfoCardBinding.bind(view)
    }
}
