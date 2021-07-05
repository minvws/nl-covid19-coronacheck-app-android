package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardPlaceholderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewGreenCardPlaceholderItem :
    BindableItem<ItemMyOverviewGreenCardPlaceholderBinding>(R.layout.item_my_overview_green_card_placeholder.toLong()) {

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_card_placeholder
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardPlaceholderBinding {
        return ItemMyOverviewGreenCardPlaceholderBinding.bind(view)
    }

    override fun bind(viewBinding: ItemMyOverviewGreenCardPlaceholderBinding, position: Int) {
        viewBinding.text.setHtmlText(
            htmlText = viewBinding.root.context.getString(R.string.my_overview_qr_placeholder_description),
            htmlLinksEnabled = true
        )
    }
}
