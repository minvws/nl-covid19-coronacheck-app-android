package nl.rijksoverheid.ctr.introduction.privacy_consent

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.ItemPrivacyConsentBinding
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.shared.ext.fromHtml

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PrivacyConsentAdapterItem(private val item: PrivacyPolicyItem) :
    BindableItem<ItemPrivacyConsentBinding>() {
    override fun getLayout(): Int {
        return R.layout.item_privacy_consent
    }

    override fun bind(viewBinding: ItemPrivacyConsentBinding, position: Int) {
        viewBinding.icon.setImageResource(item.iconResource)
        viewBinding.description.text =
            viewBinding.description.context.getString(item.textResource).fromHtml()
    }

    override fun initializeViewBinding(view: View): ItemPrivacyConsentBinding {
        return ItemPrivacyConsentBinding.bind(view)
    }

}
