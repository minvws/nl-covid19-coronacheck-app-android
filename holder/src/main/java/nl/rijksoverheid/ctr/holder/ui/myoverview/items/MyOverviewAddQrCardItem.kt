/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewAddQrBinding
import org.koin.core.component.KoinComponent

class MyOverviewAddQrCardItem(
    private val onButtonClick: () -> Unit,
) : BindableItem<ItemMyOverviewAddQrBinding>(R.layout.item_my_overview_add_qr.toLong()),
    KoinComponent {

    override fun bind(viewBinding: ItemMyOverviewAddQrBinding, position: Int) {
        viewBinding.text.setOnClickListener { onButtonClick.invoke() }
    }

    override fun getLayout(): Int = R.layout.item_my_overview_add_qr

    override fun initializeViewBinding(view: View): ItemMyOverviewAddQrBinding {
        return ItemMyOverviewAddQrBinding.bind(view)
    }
}