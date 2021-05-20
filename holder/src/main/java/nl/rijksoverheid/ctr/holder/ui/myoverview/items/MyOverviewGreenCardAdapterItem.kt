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
import nl.rijksoverheid.ctr.design.ext.formatDate
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

class MyOverviewGreenCardAdapterItem(
    private val greenCard: GreenCard,
    private val onButtonClick: () -> Unit,
) :
    BindableItem<ItemMyOverviewGreenCardBinding>(R.layout.item_my_overview_green_card.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewGreenCardBinding, position: Int) {
        applyStyling(
            viewBinding = viewBinding
        )

        setContent(
            viewBinding = viewBinding
        )

        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }
    }

    private fun applyStyling(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context
        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
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
            }
            is GreenCardType.Domestic -> {
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
        }
    }

    private fun setContent(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context

        viewBinding.proof1.visibility = View.GONE
        viewBinding.proof2.visibility = View.GONE
        viewBinding.proof3.visibility = View.GONE

        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                // European card only has one origin
                val origin = greenCard.origins.first()
                when (origin.type) {
                    is OriginType.Test -> {
                        viewBinding.proof1.text = context.getString(
                            R.string.qr_card_test_validity_domestic,
                            origin.expirationTime.toLocalDate().formatDate()
                        )
                    }
                    is OriginType.Vaccination -> {
                        viewBinding.proof1.text = context.getString(
                            R.string.qr_card_vaccination_validity_domestic,
                            origin.expirationTime.toLocalDate().formatDate()
                        )
                    }
                    is OriginType.Recovery -> {
                        viewBinding.proof1.text = context.getString(
                            R.string.qr_card_recovery_validity_domestic,
                            origin.expirationTime.toLocalDate().formatDate()
                        )
                    }
                }
                viewBinding.proof1.visibility = View.VISIBLE
            }
            is GreenCardType.Domestic -> {
                // Domestic cards can have multiple origins
                greenCard.origins.forEach { origin ->
                    when (origin.type) {
                        is OriginType.Test -> {
                            viewBinding.proof1.text = context.getString(
                                R.string.qr_card_test_validity_nl,
                                origin.expirationTime.formatDateTime(context)
                            )
                            viewBinding.proof1.visibility = View.VISIBLE
                        }
                        is OriginType.Vaccination -> {
                            viewBinding.proof2.text = context.getString(
                                R.string.qr_card_vaccination_validity_nl,
                                origin.expirationTime.formatDateTime(context)
                            )
                            viewBinding.proof2.visibility = View.VISIBLE
                        }
                        is OriginType.Recovery -> {
                            viewBinding.proof3.text = context.getString(
                                R.string.qr_card_recovery_validity_nl,
                                origin.expirationTime.formatDateTime(context)
                            )
                            viewBinding.proof3.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardBinding {
        return ItemMyOverviewGreenCardBinding.bind(view)
    }
}
