/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.design.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import nl.rijksoverheid.ctr.shared.ext.getDisplaySize

class AnimatorScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    // it's used by the object animator of the fragment transition animations
    @Suppress("unused")
    fun setPercentageX(percentage: Float) {
        val width = context.getDisplaySize().width
        x = if (width > 0) {
            percentage * width
        } else {
            0f
        }
    }
}
