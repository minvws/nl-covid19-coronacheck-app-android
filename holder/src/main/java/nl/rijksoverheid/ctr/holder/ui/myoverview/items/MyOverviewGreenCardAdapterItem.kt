/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import androidx.annotation.DimenRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.card.MaterialCardView
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem.CredentialState.HasCredential
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyOverviewGreenCardAdapterItem(
    private val cards: List<DashboardItem.CardsItem.CardItem>,
    private val onButtonClick: (greenCard: GreenCard, credentials: List<ByteArray>, credentialExpirationTimeSeconds: Long) -> Unit,
    private val onRetryClick: () -> Unit = {},
) :
    BindableItem<ItemMyOverviewGreenCardBinding>(R.layout.item_my_overview_green_card.toLong()),
    KoinComponent {

    private val myOverViewGreenCardAdapterUtil: MyOverViewGreenCardAdapterUtil by inject()

    override fun bind(viewBinding: ItemMyOverviewGreenCardBinding, position: Int) {
        applyStyling(viewBinding = viewBinding)

        setContent(viewBinding = viewBinding)

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
            description.removeAllViews()
            greenCards.removeViews(1, viewBinding.greenCards.childCount - 1)
            errorText.setHtmlText("")
            errorTextRetry.setHtmlText("")
            errorIcon.visibility = View.GONE
            errorText.visibility = View.GONE
            errorTextRetry.visibility = View.GONE
        }

        myOverViewGreenCardAdapterUtil.setContent(
            ViewBindingWrapperImpl(viewBinding),
            cards.map { it.greenCard },
            cards.first().originStates,
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
        for (i in 1 until cards.count()) {
            viewBinding.greenCards.addView(
                MaterialCardView(viewBinding.greenCards.context).apply {
                    radius = getDimensionPixel(R.dimen.dashboard_card_corner_radius)
                    cardElevation = getDimensionPixel(R.dimen.dashboard_card_elevation) -
                            (getDimensionPixel(R.dimen.dashboard_card_additional_lower_elevation) * i)
                    translationY =
                        getDimensionPixel(R.dimen.dashboard_card_additional_translation_y) * i
                },
                ConstraintLayout.LayoutParams(0, 0).apply {
                    topToTop = viewBinding.testResult.id
                    startToStart = viewBinding.testResult.id
                    endToEnd = viewBinding.testResult.id
                    bottomToBottom = viewBinding.testResult.id
                }
            )
        }
    }

    private fun MaterialCardView.getDimensionPixel(@DimenRes dimenRes: Int) =
        context.resources.getDimensionPixelSize(dimenRes).toFloat()

    private fun showError(viewBinding: ItemMyOverviewGreenCardBinding) {
        if (cards.first().credentialState is DashboardItem.CardsItem.CredentialState.NoCredential) {
            when (cards.first().databaseSyncerResult) {
                is DatabaseSyncerResult.Failed.NetworkError -> {
                    viewBinding.errorText.setHtmlText(R.string.my_overview_green_card_internet_error)
                    viewBinding.errorText.enableCustomLinks(onRetryClick)
                    viewBinding.errorTextRetry.setHtmlText("")
                    viewBinding.errorIcon.visibility = View.VISIBLE
                    viewBinding.errorText.visibility = View.VISIBLE
                    viewBinding.errorTextRetry.visibility = View.GONE
                }
                is DatabaseSyncerResult.Failed.ServerError -> {
                    viewBinding.errorText.setHtmlText(R.string.my_overview_green_card_server_error)
                    viewBinding.errorText.enableCustomLinks(onRetryClick)
                    viewBinding.errorIcon.visibility = View.VISIBLE
                    viewBinding.errorText.visibility = View.VISIBLE
                    viewBinding.errorTextRetry.visibility = View.GONE
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
