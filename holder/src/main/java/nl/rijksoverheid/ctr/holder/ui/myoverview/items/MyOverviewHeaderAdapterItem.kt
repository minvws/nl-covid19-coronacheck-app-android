package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import androidx.annotation.StringRes
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewHeaderBinding
import nl.rijksoverheid.ctr.holder.launchUrl

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class ButtonInfo(@StringRes val text: Int, @StringRes val link: Int)

class MyOverviewHeaderAdapterItem(@StringRes private val text: Int, private val buttonInfo: ButtonInfo?) :
    BindableItem<ItemMyOverviewHeaderBinding>(R.layout.item_my_overview_header.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewHeaderBinding, position: Int) {
        viewBinding.text.setHtmlText(text, htmlLinksEnabled = true)
        viewBinding.button.run {
            if (buttonInfo != null) {
                visibility = View.VISIBLE
                setText(buttonInfo.text)
                setOnClickListener {
                    context.launchUrl(context.getString(buttonInfo.link))
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_header
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewHeaderBinding {
        return ItemMyOverviewHeaderBinding.bind(view)
    }
}