package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemHolderNameSelectionFooterBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionFooterAdapterItem(
    private val onButtonClicked: () -> Unit
) : BindableItem<ItemHolderNameSelectionFooterBinding>(R.layout.item_holder_name_selection_footer.toLong()) {

    override fun bind(viewBinding: ItemHolderNameSelectionFooterBinding, position: Int) {
        viewBinding.holderNameSelectionExplainButton.setOnClickListener {
            onButtonClicked()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_holder_name_selection_footer
    }

    override fun initializeViewBinding(view: View): ItemHolderNameSelectionFooterBinding {
        return ItemHolderNameSelectionFooterBinding.bind(view)
    }
}
