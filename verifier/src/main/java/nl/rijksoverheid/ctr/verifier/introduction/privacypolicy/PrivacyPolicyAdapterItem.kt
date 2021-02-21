package nl.rijksoverheid.ctr.verifier.introduction.privacypolicy

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemPrivacyPolicyBinding
import nl.rijksoverheid.ctr.verifier.introduction.privacypolicy.models.PrivacyPolicyItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PrivacyPolicyAdapterItem(private val item: PrivacyPolicyItem) :
    BindableItem<ItemPrivacyPolicyBinding>() {
    override fun getLayout(): Int {
        return R.layout.item_privacy_policy
    }

    override fun bind(viewBinding: ItemPrivacyPolicyBinding, position: Int) {
        viewBinding.icon.setImageResource(item.iconResource)
        viewBinding.description.text =
            viewBinding.description.context.getString(item.textResource).fromHtml()
    }

    override fun initializeViewBinding(view: View): ItemPrivacyPolicyBinding {
        return ItemPrivacyPolicyBinding.bind(view)
    }

}
