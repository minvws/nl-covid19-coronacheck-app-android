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
import nl.rijksoverheid.ctr.design.ext.enableCustomLinks
import nl.rijksoverheid.ctr.design.ext.getThemeColor
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyOverviewGreenCardAdapterItem(
    private val greenCard: GreenCard,
    private val originStates: List<OriginState>,
    private val credentialState: MyOverviewItem.GreenCardItem.CredentialState,
    private val databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success,
    private val onButtonClick: (greenCard: GreenCard, credential: CredentialEntity) -> Unit,
    private val onRetryClick: () -> Unit = {},
) :
    BindableItem<ItemMyOverviewGreenCardBinding>(R.layout.item_my_overview_green_card.toLong()),
    KoinComponent {

    private val myOverViewGreenCardAdapterUtil: MyOverViewGreenCardAdapterUtil by inject()

    override fun bind(viewBinding: ItemMyOverviewGreenCardBinding, position: Int) {
        applyStyling(
            viewBinding = viewBinding
        )

        setContent(
            viewBinding = viewBinding
        )

        viewBinding.buttonWithProgressWidgetContainer.setButtonOnClickListener {
            if (credentialState is MyOverviewItem.GreenCardItem.CredentialState.HasCredential) {
                onButtonClick.invoke(greenCard, credentialState.credential)
            }
        }
    }

    private fun applyStyling(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context
        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                viewBinding.typeTitle.apply {
                    text = context.getString(R.string.validity_type_european_title)
                    setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
                viewBinding.buttonWithProgressWidgetContainer.setEnabledButtonColor(R.color.primary_blue)
                viewBinding.imageView.setImageResource(R.drawable.ic_international_card)
            }
            is GreenCardType.Domestic -> {
                viewBinding.typeTitle.apply {
                    text = context.getString(R.string.validity_type_dutch_title)
                    setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
                viewBinding.buttonWithProgressWidgetContainer.setEnabledButtonColor(R.color.primary_blue)
                viewBinding.imageView.setImageResource(R.drawable.ic_dutch_card)
            }
        }

        if (credentialState is MyOverviewItem.GreenCardItem.CredentialState.LoadingCredential) {
            viewBinding.buttonWithProgressWidgetContainer.setAccessibilityText(context.getString(R.string.my_overview_test_result_button_indicator_accessibility_description))
            viewBinding.buttonWithProgressWidgetContainer.loading()
        } else {
            viewBinding.buttonWithProgressWidgetContainer.idle(
                isEnabled = credentialState is MyOverviewItem.GreenCardItem.CredentialState.HasCredential
            )
        }

    }

    private fun setContent(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context

        viewBinding.proof1Title.visibility = View.GONE
        viewBinding.proof1Subtitle.visibility = View.GONE
        viewBinding.proof2Title.visibility = View.GONE
        viewBinding.proof2Subtitle.visibility = View.GONE
        viewBinding.proof3Title.visibility = View.GONE
        viewBinding.proof3Subtitle.visibility = View.GONE
        viewBinding.proof1Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.proof2Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.proof3Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.errorText.setHtmlText("")
        viewBinding.errorTextRetry.setHtmlText("")
        viewBinding.errorIcon.visibility = View.GONE
        viewBinding.errorText.visibility = View.GONE
        viewBinding.errorTextRetry.visibility = View.GONE

        myOverViewGreenCardAdapterUtil.setContent(greenCard, originStates, ViewBindingWrapperImpl(viewBinding))

        showError(viewBinding)
    }

    private fun showError(viewBinding: ItemMyOverviewGreenCardBinding) {
        if (credentialState is MyOverviewItem.GreenCardItem.CredentialState.NoCredential) {
            val context = viewBinding.errorText.context
            when (databaseSyncerResult) {
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
