package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewSyncGreenCardsBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewSyncGreenCardsItem(
    private val onButtonClick: () -> Unit
) :
    BindableItem<ItemMyOverviewSyncGreenCardsBinding>(R.layout.item_my_overview_sync_green_cards.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewSyncGreenCardsBinding, position: Int) {
        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_sync_green_cards
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewSyncGreenCardsBinding {
        return ItemMyOverviewSyncGreenCardsBinding.bind(view)
    }
}
