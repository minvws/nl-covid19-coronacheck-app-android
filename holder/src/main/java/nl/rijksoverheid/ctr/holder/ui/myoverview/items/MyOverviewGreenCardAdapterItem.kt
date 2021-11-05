/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem.CredentialState.HasCredential
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class AdapterCard(val greenCard: GreenCard, val originStates: List<OriginState>)

class MyOverviewGreenCardAdapterItem(
    private val cards: List<DashboardItem.CardsItem.CardItem>,
    private val onButtonClick: (greenCard: GreenCard, credentials: List<ByteArray>, credentialExpirationTimeSeconds: Long) -> Unit,
    private val onRetryClick: () -> Unit = {}
) :
    BindableItem<ItemMyOverviewGreenCardBinding>(R.layout.item_my_overview_green_card.toLong()),
    KoinComponent {

    private val myOverViewGreenCardAdapterUtil: MyOverViewGreenCardAdapterUtil by inject()

    override fun bind(viewBinding: ItemMyOverviewGreenCardBinding, position: Int) {
        applyStyling(viewBinding = viewBinding)
        setContent(viewBinding = viewBinding)
        initButton(viewBinding)
    }

    private fun initButton(viewBinding: ItemMyOverviewGreenCardBinding) {
        viewBinding.buttonWithProgressWidgetContainer.setButtonOnClickListener {
            val mainCredentialState = cards.first().credentialState
            if (mainCredentialState is HasCredential) {
                val credentials = cards.mapNotNull {
                    (it.credentialState as? HasCredential)?.credential?.data
                }
                onButtonClick.invoke(
                    cards.first().greenCard,
                    credentials,
                    mainCredentialState.credential.expirationTime.toEpochSecond()
                )
            }
        }
    }

    private fun applyStyling(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context

        viewBinding.buttonWithProgressWidgetContainer.setButtonText(
            viewBinding.root.context.getString(
                if (cards.size > 1) R.string.my_overview_results_button else R.string.my_overview_test_result_button
            )
        )

        when (cards.first().greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                viewBinding.buttonWithProgressWidgetContainer.setEnabledButtonColor(R.color.primary_blue)
                viewBinding.imageView.setImageResource(R.drawable.ic_international_card)
            }
            is GreenCardType.Domestic -> {
                viewBinding.buttonWithProgressWidgetContainer.setEnabledButtonColor(R.color.primary_blue)
                viewBinding.imageView.setImageResource(R.drawable.ic_dutch_card)
            }
        }

        if (cards.first().credentialState is DashboardItem.CardsItem.CredentialState.LoadingCredential) {
            viewBinding.buttonWithProgressWidgetContainer.setAccessibilityText(context.getString(R.string.my_overview_test_result_button_indicator_accessibility_description))
            viewBinding.buttonWithProgressWidgetContainer.loading()
        } else {
            viewBinding.buttonWithProgressWidgetContainer.idle(
                isEnabled = cards.first().credentialState is HasCredential
            )
        }

    }

    private fun setContent(viewBinding: ItemMyOverviewGreenCardBinding) {
        // reset layout
        viewBinding.run {
            (proof2.layoutParams as ViewGroup.MarginLayoutParams).height = 0
            (proof3.layoutParams as ViewGroup.MarginLayoutParams).height = 0
            description.removeAllViews()
            errorContainer.visibility = View.GONE
        }

        myOverViewGreenCardAdapterUtil.setContent(
            ViewBindingWrapperImpl(viewBinding),
            cards.map { AdapterCard(it.greenCard, it.originStates) }
                .sortedByDescending { it.originStates.first().origin.eventTime },
        )

        stackAdditionalCards(viewBinding)

        showError(viewBinding)
    }

    /**
     * Show a border of extra cards when item has additional items of the same type
     *
     * @param[viewBinding] view binding containing binding of parent view group of green cards
     */
    private fun stackAdditionalCards(viewBinding: ItemMyOverviewGreenCardBinding) {
        viewBinding.apply {
            if (cards.size >= 2) {
                (proof2.layoutParams as ViewGroup.MarginLayoutParams).height = viewBinding.root.context.resources.getDimensionPixelSize(R.dimen.dashboard_card_additional_card_height)
            }

            if (cards.size >= 3) {
                (proof3.layoutParams as ViewGroup.MarginLayoutParams).height = viewBinding.root.context.resources.getDimensionPixelSize(R.dimen.dashboard_card_additional_card_height)
            }
        }
    }

    private fun showError(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context
        if (cards.first().credentialState is DashboardItem.CardsItem.CredentialState.NoCredential) {
            when (cards.first().databaseSyncerResult) {
                is DatabaseSyncerResult.Failed.NetworkError -> {
                    viewBinding.errorText.setHtmlText(
                        htmlText = context.getString(R.string.my_overview_green_card_internet_error),
                        htmlTextColor = ContextCompat.getColor(context, R.color.error),
                        htmlTextColorLink = ContextCompat.getColor(context, R.color.error)
                    )
                    viewBinding.errorText.enableCustomLinks(onRetryClick)
                    viewBinding.errorContainer.visibility = View.VISIBLE
                }
                is DatabaseSyncerResult.Failed.ServerError.FirstTime -> {
                    viewBinding.errorText.setHtmlText(
                        htmlText = context.getString(R.string.my_overview_green_card_server_error),
                        htmlTextColor = ContextCompat.getColor(context, R.color.error),
                        htmlTextColorLink = ContextCompat.getColor(context, R.color.error)
                    )
                    viewBinding.errorText.enableCustomLinks(onRetryClick)
                    viewBinding.errorContainer.visibility = View.VISIBLE
                }
                is DatabaseSyncerResult.Failed.ServerError.MultipleTimes -> {
                    viewBinding.errorText.setHtmlText(
                        htmlText = context.getString(R.string.my_overview_green_card_server_error_after_retry),
                        htmlTextColor = ContextCompat.getColor(context, R.color.error),
                        htmlTextColorLink = ContextCompat.getColor(context, R.color.error)
                    )
                    viewBinding.errorContainer.visibility = View.VISIBLE
                }
                else -> {

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
