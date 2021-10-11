package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.annotation.StringRes
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewHeaderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewHeaderAdapterItem(@StringRes private val text: Int, private val showButton: Boolean) :
    BindableItem<ItemMyOverviewHeaderBinding>(R.layout.item_my_overview_header.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewHeaderBinding, position: Int) {
        viewBinding.text.setHtmlText(text, htmlLinksEnabled = true)
        viewBinding.button.run {
            if (showButton) {
                visibility = View.VISIBLE
                setOnClickListener {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(viewBinding.root.context.getString(R.string.my_overview_description_eu_button_link))
                        )
                    )
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