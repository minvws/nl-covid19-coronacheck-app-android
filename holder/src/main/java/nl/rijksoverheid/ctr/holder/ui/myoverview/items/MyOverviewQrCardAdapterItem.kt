/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewQrCardBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents.Event

class MyOverviewQrCardAdapterItem(
    private val events: List<Event>,
    private val isEuropean: Boolean,
    private val onButtonClick: () -> Unit,
) :
    BindableItem<ItemMyOverviewQrCardBinding>(R.layout.item_my_overview_qr_card.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewQrCardBinding, position: Int) {
        val context = viewBinding.root.context
        if (isEuropean) {
            viewBinding.typeTitle.apply {
                text = context.getString(R.string.validity_type_european_title)
                setTextColor(ContextCompat.getColor(context, R.color.darkened_blue))
            }
            viewBinding.button.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.darkened_blue
                )
            )
        } else {
            viewBinding.typeTitle.apply {
                text = context.getString(R.string.validity_type_dutch_title)
                setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
            }
            viewBinding.button.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.primary_blue
                )
            )
        }

        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }

        handleEvents(viewBinding)

    }

    private fun handleEvents(viewBinding: ItemMyOverviewQrCardBinding) {
        val context = viewBinding.root.context
        val inflater = LayoutInflater.from(context)
        events.forEach {
            val label = inflater.inflate(
                R.layout.view_qr_card_event_label,
                viewBinding.resultsContainer
            ) as TextView
            when (it.type) {
                "vaccination" -> {
                    if (isEuropean) {
                        label.text = context.getString(R.string.qr_card_vaccination_validity_europe)
                    } else {
                        label.text = context.getString(R.string.qr_card_vaccination_validity_nl)
                    }
                }
                "recovery" -> {
                    if (isEuropean) {
                        label.text = context.getString(R.string.qr_card_recovery_validity_europe)
                    } else {
                        label.text = context.getString(R.string.qr_card_recovery_validity_nl)
                    }
                }
                "test" -> {
                    if (isEuropean) {
                        label.text = context.getString(R.string.qr_card_test_validity_europe)
                    } else {
                        label.text = context.getString(R.string.qr_card_test_validity_nl)
                    }
                }
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