package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewCoronamelderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewCoronaMelderAdapterItem: BindableItem<ItemMyOverviewCoronamelderBinding>(R.layout.item_my_overview_coronamelder.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewCoronamelderBinding, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_coronamelder
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewCoronamelderBinding {
        return ItemMyOverviewCoronamelderBinding.bind(view)
    }
}