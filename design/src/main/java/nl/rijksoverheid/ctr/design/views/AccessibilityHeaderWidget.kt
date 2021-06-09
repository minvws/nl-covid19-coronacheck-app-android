/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityHeading

class AccessibilityHeaderWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : MaterialTextView(context, attrs, defStyle, defStyleRes) {

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AccessibilityHeaderWidget,
            0, 0
        ).apply {
            try {
                val isHeader =
                    getBoolean(R.styleable.AccessibilityHeaderWidget_isAccessibilityHeader, true)
                setAsAccessibilityHeading(isHeader)
            } finally {
                recycle()
            }
        }
    }

}