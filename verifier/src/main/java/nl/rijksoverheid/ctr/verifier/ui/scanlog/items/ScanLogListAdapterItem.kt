package nl.rijksoverheid.ctr.verifier.ui.scanlog.items

import android.annotation.SuppressLint
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.ext.tintDrawable
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemScanLogListItemBinding
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.util.ScanLogListAdapterItemUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogListAdapterItem(val item: ScanLogItem.ListScanLogItem): BindableItem<ItemScanLogListItemBinding>(
    R.layout.item_scan_log_list_item.toLong()), KoinComponent {

    private val util: ScanLogListAdapterItemUtil by inject()

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: ItemScanLogListItemBinding, position: Int) {
        val context = viewBinding.root.context
        val scanLog = item.scanLog

        // Apply correct color to icon that is part of the text
        viewBinding.skew.tintDrawable(R.color.error)

        viewBinding.type.text = item.scanLog.policy.configValue
        viewBinding.time.text = util.getTimeString(context, scanLog.from, scanLog.to, item.index == 0)
        viewBinding.amount.text = util.getAmountString(context, scanLog.count)

        if (scanLog.skew) {
            viewBinding.skew.visibility = View.VISIBLE
        } else {
            viewBinding.skew.visibility = View.GONE
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_scan_log_list_item
    }

    override fun initializeViewBinding(view: View): ItemScanLogListItemBinding {
        return ItemScanLogListItemBinding.bind(view)
    }
}