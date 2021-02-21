package nl.rijksoverheid.ctr.holder.myoverview.items

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewNavigationCardBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewNavigationCardAdapterItem(
    @StringRes private val title: Int,
    @StringRes private val description: Int,
    @ColorRes private val backgroundColor: Int,
    @DrawableRes private val backgroundDrawable: Int,
    @StringRes private val buttonText: Int,
    private val onButtonClick: () -> Unit
) : BindableItem<ItemMyOverviewNavigationCardBinding>(R.layout.item_my_overview_navigation_card.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewNavigationCardBinding, position: Int) {
        viewBinding.title.setText(title)
        viewBinding.description.setText(description)
        viewBinding.container.setBackgroundColor(backgroundColor)
        viewBinding.image.setImageResource(backgroundDrawable)
        viewBinding.button.setText(buttonText)
        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_navigation_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewNavigationCardBinding {
        return ItemMyOverviewNavigationCardBinding.bind(view)
    }
}
