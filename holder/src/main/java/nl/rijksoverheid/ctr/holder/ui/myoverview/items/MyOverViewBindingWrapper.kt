package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.widget.TextView
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ViewBindingWrapper {
    val proof1Title: TextView
    val proof2Title: TextView
    val proof3Title: TextView
    val proof1Subtitle: TextView
    val proof2Subtitle: TextView
    val proof3Subtitle: TextView
    val expiresIn: TextView
}

class ViewBindingWrapperImpl(private val viewBinding: ItemMyOverviewGreenCardBinding): ViewBindingWrapper {
    override val proof1Title: TextView
        get() = viewBinding.proof1Title

    override val proof2Title: TextView
        get() = viewBinding.proof2Title
    override val proof3Title: TextView
        get() = viewBinding.proof3Title
    override val proof1Subtitle: TextView
        get() = viewBinding.proof1Subtitle
    override val proof2Subtitle: TextView
        get() = viewBinding.proof2Subtitle
    override val proof3Subtitle: TextView
        get() = viewBinding.proof3Subtitle
    override val expiresIn: TextView
        get() = viewBinding.expiresIn
}