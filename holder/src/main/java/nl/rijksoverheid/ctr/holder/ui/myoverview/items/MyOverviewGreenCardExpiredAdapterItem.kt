package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardExpiredBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewGreenCardExpiredAdapterItem(
    private val greenCard: GreenCard,
    private val onDismissClick: (item: BindableItem<*>, greenCard: GreenCard) -> Unit
) :
    BindableItem<ItemMyOverviewGreenCardExpiredBinding>(R.layout.item_my_overview_green_card_expired.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewGreenCardExpiredBinding, position: Int) {
        viewBinding.close.setOnClickListener {
            onDismissClick.invoke(this, greenCard)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_card_expired
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardExpiredBinding {
        return ItemMyOverviewGreenCardExpiredBinding.bind(view)
    }
}
