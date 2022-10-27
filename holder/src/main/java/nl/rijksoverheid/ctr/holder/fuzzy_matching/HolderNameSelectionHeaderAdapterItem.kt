package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemHolderNameSelectionHeaderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionHeaderAdapterItem : BindableItem<ItemHolderNameSelectionHeaderBinding>(R.layout.item_holder_name_selection_header.toLong()) {
    override fun bind(viewBinding: ItemHolderNameSelectionHeaderBinding, position: Int) {
    }

    override fun getLayout(): Int {
        return R.layout.item_holder_name_selection_header
    }

    override fun initializeViewBinding(view: View): ItemHolderNameSelectionHeaderBinding {
        return ItemHolderNameSelectionHeaderBinding.bind(view)
    }
}
