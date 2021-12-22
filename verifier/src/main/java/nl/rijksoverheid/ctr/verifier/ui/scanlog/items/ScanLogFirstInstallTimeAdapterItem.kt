package nl.rijksoverheid.ctr.verifier.ui.scanlog.items

import android.annotation.SuppressLint
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemScanLogFirstInstallTimeBinding
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.util.ScanLogFirstInstallTimeAdapterItemUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogFirstInstallTimeAdapterItem(private val item: ScanLogItem.FirstInstallTimeItem): BindableItem<ItemScanLogFirstInstallTimeBinding>(
    R.layout.item_scan_log_first_install_time.toLong()), KoinComponent {

    private val util: ScanLogFirstInstallTimeAdapterItemUtil by inject()

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: ItemScanLogFirstInstallTimeBinding, position: Int) {
        viewBinding.text.text =  util.getFirstInstallTimeString(
            context = viewBinding.root.context,
            firstInstallTime = item.firstInstallTime
        )
    }

    override fun getLayout(): Int {
        return R.layout.item_scan_log_first_install_time
    }

    override fun initializeViewBinding(view: View): ItemScanLogFirstInstallTimeBinding {
        return ItemScanLogFirstInstallTimeBinding.bind(view)
    }
}