package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardsSyncedBinding
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewSyncGreenCardsBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewGreenCardsSyncedItem(
    private val onCloseClick: (item: MyOverviewGreenCardsSyncedItem) -> Unit,
    private val onButtonClick: () -> Unit
) :
    BindableItem<ItemMyOverviewGreenCardsSyncedBinding>(R.layout.item_my_overview_green_cards_synced.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewGreenCardsSyncedBinding, position: Int) {
        viewBinding.close.setOnClickListener {
            onCloseClick.invoke(this)
        }
        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_cards_synced
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardsSyncedBinding {
        return ItemMyOverviewGreenCardsSyncedBinding.bind(view)
    }
}