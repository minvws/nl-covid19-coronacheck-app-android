/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewClockDeviationBinding

class MyOverviewClockDeviationItem(private val onInfoIconClicked: () -> Unit) :
    BindableItem<ItemMyOverviewClockDeviationBinding>(R.layout.item_my_overview_clock_deviation.toLong()) {

    override fun getLayout(): Int {
        return R.layout.item_my_overview_clock_deviation
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewClockDeviationBinding {
        return ItemMyOverviewClockDeviationBinding.bind(view)
    }

    override fun bind(viewBinding: ItemMyOverviewClockDeviationBinding, position: Int) {
        viewBinding.info.setOnClickListener {
            onInfoIconClicked.invoke()
        }
    }
}