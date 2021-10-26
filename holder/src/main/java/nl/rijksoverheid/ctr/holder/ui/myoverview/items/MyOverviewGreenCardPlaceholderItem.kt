package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Intent
import android.net.Uri
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardPlaceholderBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewGreenCardPlaceholderItem(private val greenCardType: GreenCardType) :
    BindableItem<ItemMyOverviewGreenCardPlaceholderBinding>(R.layout.item_my_overview_green_card_placeholder.toLong()) {

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_card_placeholder
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardPlaceholderBinding {
        return ItemMyOverviewGreenCardPlaceholderBinding.bind(view)
    }

    override fun bind(viewBinding: ItemMyOverviewGreenCardPlaceholderBinding, position: Int) {
        val isEu = greenCardType == GreenCardType.Eu

        viewBinding.icon.setBackgroundResource(if (isEu) {
            R.drawable.ic_illustration_hand_qr_placeholder_eu
        } else {
            R.drawable.ic_illustration_hand_qr_placeholder
        })
        viewBinding.title.text = viewBinding.title.context.getString(if (isEu) {
            R.string.my_overview_qr_placeholder_header_eu
        } else {
            R.string.my_overview_qr_placeholder_header
        })
    }
}
