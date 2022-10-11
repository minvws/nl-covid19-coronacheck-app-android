package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.view.View
import androidx.core.view.isVisible
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemHolderNameSelectionViewBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionViewAdapterItem(
    private val item: HolderNameSelectionItem.ListItem,
    private val onDetailsButtonClicked: () -> Unit,
    private val onSelected: (Int) -> Unit
) : BindableItem<ItemHolderNameSelectionViewBinding>(
    R.layout.item_holder_name_selection_view.toLong()
) {

    override fun bind(viewBinding: ItemHolderNameSelectionViewBinding, position: Int) {
        viewBinding.radioButton.isChecked = item.isSelected
        viewBinding.nameTextView.text = item.name
        viewBinding.eventsTextView.text = item.events
        viewBinding.removedEventTextView.isVisible = item.willBeRemoved
        viewBinding.root.setOnClickListener {
            onSelected(position)
        }
        viewBinding.detailsButton.setOnClickListener {
            onDetailsButtonClicked()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_holder_name_selection_view
    }

    override fun initializeViewBinding(view: View): ItemHolderNameSelectionViewBinding {
        return ItemHolderNameSelectionViewBinding.bind(view)
    }
}
