package nl.rijksoverheid.ctr.verifier.ui.scanlog.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemScanLogHeaderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogHeaderAdapterItem(val item: ScanLogItem.HeaderItem): BindableItem<ItemScanLogHeaderBinding>(R.layout.item_scan_log_header.toLong()) {

    override fun bind(viewBinding: ItemScanLogHeaderBinding, position: Int) {
        viewBinding.text.setHtmlText(viewBinding.root.context.getString(R.string.scan_log_message, item.scanLogStorageMinutes), true)
    }

    override fun getLayout(): Int {
        return R.layout.item_scan_log_header
    }

    override fun initializeViewBinding(view: View): ItemScanLogHeaderBinding {
        return ItemScanLogHeaderBinding.bind(view)
    }
}