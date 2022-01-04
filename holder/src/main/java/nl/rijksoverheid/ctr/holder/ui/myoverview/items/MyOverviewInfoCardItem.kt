package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewInfoCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
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
    private val onDismiss: (infoCardItem: MyOverviewInfoCardItem, infoItem: DashboardItem.InfoItem) -> Unit = { _, _ -> }
) :
    BindableItem<ItemMyOverviewInfoCardBinding>(R.layout.item_my_overview_info_card.toLong()) {

    override fun bind(viewBinding: ItemMyOverviewInfoCardBinding, position: Int) {
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
            visibility = if (infoItem.hasButton) View.VISIBLE else View.GONE
            setText(infoItem.buttonText ?: R.string.my_overview_info_card_button_read_more)
        }

        when (infoItem) {
            is DashboardItem.InfoItem.ExtendDomesticRecovery -> {
                viewBinding.text.setText(R.string.extend_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.RecoverDomesticRecovery -> {
                viewBinding.text.setText(R.string.recover_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.ConfigFreshnessWarning -> {
                viewBinding.text.setText(R.string.config_warning_card_message)
            }
            is DashboardItem.InfoItem.ExtendedDomesticRecovery -> {
                viewBinding.text.setText(R.string.extended_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.RecoveredDomesticRecovery -> {
                viewBinding.text.setText(R.string.recovered_domestic_recovery_green_card_info_card_text)
            }
            is DashboardItem.InfoItem.ClockDeviationItem -> {
                viewBinding.text.setText(R.string.my_overview_clock_deviation_description)
            }
            is DashboardItem.InfoItem.GreenCardExpiredItem -> {
                viewBinding.text.setText(R.string.qr_card_expired)
            }
            is DashboardItem.InfoItem.OriginInfoItem -> {
                viewBinding.text.text =
                    getOriginInfoText(infoItem, viewBinding.dashboardItemInfoRoot.context)
            }
            is DashboardItem.InfoItem.MissingDutchVaccinationItem -> {
                viewBinding.text.text =
                    viewBinding.text.context.getString(R.string.missing_dutch_certificate_info_card_text)
            }
            is DashboardItem.InfoItem.TestCertificate3GValidity -> {
                viewBinding.text.text =
                    viewBinding.text.context.getString(R.string.holder_my_overview_3g_test_validity_card)
            }
            is DashboardItem.InfoItem.AppUpdate -> {
                viewBinding.text.setText(R.string.recommended_update_card_description)
            }
            is DashboardItem.InfoItem.NewValidityItem -> {
                viewBinding.text.setText(R.string.holder_dashboard_newvaliditybanner_title)
            }
        }

        viewBinding.button.setOnClickListener {
            onButtonClick.invoke(infoItem)
        }
    }

    private fun getOriginInfoText(
        infoItem: DashboardItem.InfoItem.OriginInfoItem,
        context: Context
    ): String {
        val originString = when (infoItem.originType) {
            is OriginType.Vaccination -> context.getString(R.string.type_vaccination)
            is OriginType.Recovery -> context.getString(R.string.type_recovery)
            is OriginType.Test -> context.getString(R.string.type_test)
            is OriginType.VaccinationAssessment -> context.getString(R.string.type_vaccination)
        }

        return when (infoItem.greenCardType) {
            is GreenCardType.Domestic -> {
                context.getString(
                    R.string.my_overview_not_valid_domestic_but_is_in_eu, originString
                )
            }
            is GreenCardType.Eu -> {
                context.getString(
                    R.string.my_overview_not_valid_eu_but_is_in_domestic, originString
                )
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_info_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewInfoCardBinding {
        return ItemMyOverviewInfoCardBinding.bind(view)
    }
}
