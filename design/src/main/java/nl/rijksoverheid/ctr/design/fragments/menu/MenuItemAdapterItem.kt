/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.design.fragments.menu

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.ItemMenuBinding

class MenuItemAdapterItem(
    private val menuItem: MenuSection
): BindableItem<ItemMenuBinding>() {

    override fun bind(viewBinding: ItemMenuBinding, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getLayout(): Int {
        return R.layout.item_menu
    }

    override fun initializeViewBinding(view: View): ItemMenuBinding {
        return ItemMenuBinding.bind(view)
    }
}