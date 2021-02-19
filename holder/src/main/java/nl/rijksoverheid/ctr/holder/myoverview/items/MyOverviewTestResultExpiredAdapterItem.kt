package nl.rijksoverheid.ctr.holder.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewTestResultExpiredBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTestResultExpiredAdapterItem(private val onDismissClick: () -> Unit) :
    BindableItem<ItemMyOverviewTestResultExpiredBinding>(R.layout.item_my_overview_test_result_expired.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewTestResultExpiredBinding, position: Int) {
        viewBinding.close.setOnClickListener {
            onDismissClick.invoke()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_test_result_expired
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewTestResultExpiredBinding {
        return ItemMyOverviewTestResultExpiredBinding.bind(view)
    }
}
