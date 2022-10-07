package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemHolderNameSelectionFooterBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionFooterAdapterItem :
    BindableItem<ItemHolderNameSelectionFooterBinding>(R.layout.item_holder_name_selection_footer.toLong()),
    KoinComponent {

    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun bind(viewBinding: ItemHolderNameSelectionFooterBinding, position: Int) {
        viewBinding.holderNameSelectionExplainButton.setOnClickListener {
            onButtonClicked(viewBinding)
        }
    }

    private fun onButtonClicked(viewBinding: ItemHolderNameSelectionFooterBinding) =
        (viewBinding.root.context as? AppCompatActivity)?.let { activity ->
            infoFragmentUtil.presentAsBottomSheet(
                fragmentManager = activity.supportFragmentManager,
                data = InfoFragmentData.TitleDescription(
                    title = activity.getString(R.string.holder_fuzzyMatching_why_title),
                    descriptionData = DescriptionData(
                        htmlText = R.string.holder_fuzzyMatching_why_body,
                        htmlLinksEnabled = true
                    )
                )
            )
        }

    override fun getLayout(): Int {
        return R.layout.item_holder_name_selection_footer
    }

    override fun initializeViewBinding(view: View): ItemHolderNameSelectionFooterBinding {
        return ItemHolderNameSelectionFooterBinding.bind(view)
    }
}
