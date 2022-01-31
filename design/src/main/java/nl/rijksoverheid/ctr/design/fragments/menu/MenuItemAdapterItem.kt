/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.design.fragments.menu

import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.ItemMenuBinding

class MenuItemAdapterItem(
    private val menuItem: MenuSection.MenuItem,
    private val lastItemInSection: Boolean,
    private val onClick: (onClick: MenuSection.MenuItem.OnClick) -> Unit
): BindableItem<ItemMenuBinding>() {

    override fun bind(viewBinding: ItemMenuBinding, position: Int) {
        val context = viewBinding.root.context

        viewBinding.icon.setImageResource(menuItem.icon)
        viewBinding.title.setText(menuItem.title)
        viewBinding.root.setOnClickListener {
            onClick.invoke(menuItem.onClick)
        }

        val marginLayoutParams = viewBinding.root.layoutParams as ViewGroup.MarginLayoutParams
        val marginBottom = if (lastItemInSection) context.resources.getDimensionPixelSize(R.dimen.menu_section_spacing) else context.resources.getDimensionPixelSize(R.dimen.menu_list_item_spacing)
        marginLayoutParams.bottomMargin = marginBottom
    }

    override fun getLayout(): Int {
        return R.layout.item_menu
    }

    override fun initializeViewBinding(view: View): ItemMenuBinding {
        return ItemMenuBinding.bind(view)
    }
}