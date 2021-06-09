package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewOriginInfoBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewOriginInfoAdapterItem(
    private val greenCardType: GreenCardType,
    private val originType: OriginType,
    private val onInfoClick: (greenCardType: GreenCardType, originType: OriginType) -> Unit
) :
    BindableItem<ItemMyOverviewOriginInfoBinding>(R.layout.item_my_overview_origin_info.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewOriginInfoBinding, position: Int) {
        val context = viewBinding.root.context

        val originString = when (originType) {
            is OriginType.Vaccination -> context.getString(R.string.type_vaccination)
            is OriginType.Recovery -> context.getString(R.string.type_recovery)
            is OriginType.Test -> context.getString(R.string.type_test)
        }

        when (greenCardType) {
            is GreenCardType.Domestic -> {
                viewBinding.text.text = context.getString(R.string.my_overview_not_valid_domestic_but_is_in_eu, originString)
            }
            is GreenCardType.Eu -> {
                viewBinding.text.text = context.getString(R.string.my_overview_not_valid_eu_but_is_in_domestic, originString)
            }
        }
        viewBinding.info.setOnClickListener {
            onInfoClick.invoke(greenCardType, originType)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_origin_info
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewOriginInfoBinding {
        return ItemMyOverviewOriginInfoBinding.bind(view)
    }
}
