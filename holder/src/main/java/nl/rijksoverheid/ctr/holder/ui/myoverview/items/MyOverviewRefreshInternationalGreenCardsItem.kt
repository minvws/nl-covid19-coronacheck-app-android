package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewRefreshInternationalGreenCardsBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewRefreshInternationalGreenCardsItem(
    private val onButtonClick: () -> Unit
) :
    BindableItem<ItemMyOverviewRefreshInternationalGreenCardsBinding>(R.layout.item_my_overview_refresh_international_green_cards.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewRefreshInternationalGreenCardsBinding, position: Int) {
        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_refresh_international_green_cards
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewRefreshInternationalGreenCardsBinding {
        return ItemMyOverviewRefreshInternationalGreenCardsBinding.bind(view)
    }
}
