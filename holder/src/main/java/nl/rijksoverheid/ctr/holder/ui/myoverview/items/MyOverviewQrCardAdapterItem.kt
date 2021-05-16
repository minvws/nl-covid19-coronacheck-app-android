/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import androidx.core.content.ContextCompat
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewHeaderBinding
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewQrCardBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents.Event
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResult

class MyOverviewQrCardAdapterItem(
    private val events : List<Event>,
    private val isEuropean : Boolean,
    private val onButtonClick: () -> Unit,
) :
    BindableItem<ItemMyOverviewQrCardBinding>(R.layout.item_my_overview_qr_card.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewQrCardBinding, position: Int) {
        val context = viewBinding.root.context
            viewBinding.typeTitle.apply {
                if(isEuropean) {
                    text = context.getString(R.string.validity_type_european_title)
                    setTextColor(ContextCompat.getColor(context, R.color.darkened_blue))
                } else {
                    text = context.getString(R.string.validity_type_dutch_title)
                    setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
            }

    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_qr_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewQrCardBinding {
        return ItemMyOverviewQrCardBinding.bind(view)
    }
}