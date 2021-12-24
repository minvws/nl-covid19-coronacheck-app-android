package nl.rijksoverheid.ctr.verifier.ui.scanlog.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemScanLogListHeaderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogListHeaderAdapterItem(val item: ScanLogItem.ListHeaderItem): BindableItem<ItemScanLogListHeaderBinding>(
    R.layout.item_scan_log_list_header.toLong()) {

    override fun bind(viewBinding: ItemScanLogListHeaderBinding, position: Int) {
        viewBinding.text.text = viewBinding.root.context.getString(R.string.scan_log_list_header, item.scanLogStorageMinutes)
    }

    override fun getLayout(): Int {
        return R.layout.item_scan_log_list_header
    }

    override fun initializeViewBinding(view: View): ItemScanLogListHeaderBinding {
        return ItemScanLogListHeaderBinding.bind(view)
    }
}