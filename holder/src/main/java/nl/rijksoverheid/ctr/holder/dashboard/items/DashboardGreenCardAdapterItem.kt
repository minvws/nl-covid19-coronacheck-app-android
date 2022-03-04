/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemDashboardGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem.CredentialState.HasCredential
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class AdapterCard(
    val greenCard: GreenCard,
    val originStates: List<OriginState>,
    val disclosurePolicy: GreenCardDisclosurePolicy)

class DashboardGreenCardAdapterItem(
    private val cards: List<DashboardItem.CardsItem.CardItem>,
    private val onButtonClick: (cardItem: DashboardItem.CardsItem.CardItem, credentials: List<ByteArray>, credentialExpirationTimeSeconds: Long) -> Unit,
    private val onRetryClick: () -> Unit = {}
) :
    BindableItem<AdapterItemDashboardGreenCardBinding>(R.layout.adapter_item_dashboard_green_card.toLong()),
    KoinComponent {

    private val dashboardGreenCardAdapterItemUtil: DashboardGreenCardAdapterItemUtil by inject()

    override fun bind(viewBinding: AdapterItemDashboardGreenCardBinding, position: Int) {
        applyStyling(viewBinding = viewBinding)
        setContent(viewBinding = viewBinding)
        initButton(
            viewBinding = viewBinding,
            card = cards.first()
        )
        accessibility(
            viewBinding = viewBinding,
            greenCardType = cards.first().greenCard.greenCardEntity.type
        )
    }

    private fun accessibility(viewBinding: AdapterItemDashboardGreenCardBinding, greenCardType: GreenCardType) {
        viewBinding.buttonWithProgressWidgetContainer.accessibility(
            viewBinding.title.text.toString()
        )
        val imageContentDescription = viewBinding.root.context.getString(
            when (greenCardType) {
                GreenCardType.Domestic -> R.string.validity_type_dutch_title
                GreenCardType.Eu -> R.string.validity_type_european_title
            }
        )
        viewBinding.headerContainer.contentDescription = "${viewBinding.title.text} $imageContentDescription"
        // Mark title of the cards as heading for accessibility
        ViewCompat.setAccessibilityHeading(viewBinding.title, true)
    }

    private fun initButton(viewBinding: AdapterItemDashboardGreenCardBinding, card: DashboardItem.CardsItem.CardItem) {
        when (card.greenCardEnabledState) {
            is GreenCardEnabledState.Enabled -> {
                viewBinding.buttonWithProgressWidgetContainer.visibility = View.VISIBLE
                viewBinding.disabledState.visibility = View.GONE
                viewBinding.buttonWithProgressWidgetContainer.setButtonOnClickListener {
                    val mainCredentialState = cards.first().credentialState
                    if (mainCredentialState is HasCredential) {
                        val credentials = cards.mapNotNull {
                            (it.credentialState as? HasCredential)?.credential?.data
                        }
                        onButtonClick.invoke(
                            cards.first(),
                            credentials,
                            mainCredentialState.credential.expirationTime.toEpochSecond()
                        )
                    }
                }
            }
            is GreenCardEnabledState.Disabled -> {
                viewBinding.buttonWithProgressWidgetContainer.visibility = View.GONE
                viewBinding.disabledState.visibility = View.VISIBLE
                viewBinding.disabledState.setText(card.greenCardEnabledState.text)
            }
        }
    }

    private fun applyStyling(viewBinding: AdapterItemDashboardGreenCardBinding) {
        viewBinding.buttonWithProgressWidgetContainer.setButtonText(
            viewBinding.root.context.getString(
                if (cards.size > 1) R.string.my_overview_results_button else R.string.my_overview_test_result_button
            )
        )

        val card = cards.first()

        when (card.greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                viewBinding.internationalImageContainer.visibility = View.VISIBLE
                viewBinding.domesticImageContainer.visibility = View.GONE
                viewBinding.buttonWithProgressWidgetContainer.setEnabledButtonColor(R.color.primary_blue)
            }
            is GreenCardType.Domestic -> {
                viewBinding.internationalImageContainer.visibility = View.GONE
                viewBinding.domesticImageContainer.visibility = View.VISIBLE
                viewBinding.buttonWithProgressWidgetContainer.setEnabledButtonColor(R.color.primary_blue)
            }
        }

        if (cards.first().credentialState is DashboardItem.CardsItem.CredentialState.LoadingCredential) {
            viewBinding.buttonWithProgressWidgetContainer.loading()
        } else {
            viewBinding.buttonWithProgressWidgetContainer.idle(
                isEnabled = cards.first().credentialState is HasCredential
            )
        }
    }

    private fun setContent(viewBinding: AdapterItemDashboardGreenCardBinding) {
        // reset layout
        viewBinding.run {
            (proof2.layoutParams as ViewGroup.MarginLayoutParams).height = 0
            (proof3.layoutParams as ViewGroup.MarginLayoutParams).height = 0
            description.removeAllViews()
            errorContainer.visibility = View.GONE
        }

        dashboardGreenCardAdapterItemUtil.setContent(
            DashboardGreenCardAdapterItemBindingWrapperImpl(viewBinding),
            cards.map { AdapterCard(it.greenCard, it.originStates, it.disclosurePolicy) }
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
    private fun stackAdditionalCards(viewBinding: AdapterItemDashboardGreenCardBinding) {
        viewBinding.apply {
            if (cards.size >= 2) {
                (proof2.layoutParams as ViewGroup.MarginLayoutParams).height = viewBinding.root.context.resources.getDimensionPixelSize(R.dimen.dashboard_card_additional_card_height)
            }

            if (cards.size >= 3) {
                (proof3.layoutParams as ViewGroup.MarginLayoutParams).height = viewBinding.root.context.resources.getDimensionPixelSize(R.dimen.dashboard_card_additional_card_height)
            }
        }
    }

    private fun showError(viewBinding: AdapterItemDashboardGreenCardBinding) {
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
        return R.layout.adapter_item_dashboard_green_card
    }

    override fun initializeViewBinding(view: View): AdapterItemDashboardGreenCardBinding {
        return AdapterItemDashboardGreenCardBinding.bind(view)
    }
}
