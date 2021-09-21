package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.IncludeContentButtonBinding
import nl.rijksoverheid.ctr.shared.utils.Accessibility

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun IncludeContentButtonBinding.bind(
    @StringRes title: Int,
    subtitle: String?,
    @DrawableRes logo: Int? = null,
    onClick: () -> Unit
) {
    providerTitle.setText(title)
    providerSubtitle.text = subtitle

    if (subtitle.isNullOrEmpty()) {
        providerSubtitle.visibility = View.GONE
        providerTitle.setPadding(
            providerTitle.paddingLeft,
            providerTitle.context.resources.getDimensionPixelSize(R.dimen.test_provider_title_without_subtitle_padding),
            providerTitle.paddingRight,
            providerTitle.context.resources.getDimensionPixelSize(R.dimen.test_provider_title_without_subtitle_padding)
        )
        root.contentDescription = providerTitle.text
    } else {
        root.contentDescription = String.format("%s. %s", providerTitle.text, providerSubtitle.text)
        logo?.let { providerSubtitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, it, 0) }
    }

    Accessibility.button(root)

    root.setOnClickListener {
        onClick()
    }
}
