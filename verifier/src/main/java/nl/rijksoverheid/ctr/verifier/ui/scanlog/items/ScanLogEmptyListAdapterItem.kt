package nl.rijksoverheid.ctr.verifier.ui.scanlog.items

import android.annotation.SuppressLint
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemScanLogEmptyListBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogEmptyListAdapterItem: BindableItem<ItemScanLogEmptyListBinding>(
    R.layout.item_scan_log_empty_list.toLong()) {

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: ItemScanLogEmptyListBinding, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.item_scan_log_empty_list
    }

    override fun initializeViewBinding(view: View): ItemScanLogEmptyListBinding {
        return ItemScanLogEmptyListBinding.bind(view)
    }
}