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
import android.view.Gravity
import android.widget.LinearLayout

class IdentifierHolderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    init {
        gravity = Gravity.CENTER_HORIZONTAL
        orientation = HORIZONTAL
    }

    fun addIdentifierItems(items: List<String>, showPosition: Boolean? = false) {
        items.forEachIndexed { index, content ->
            val item = IdentifierItemView(context)
            item.setContent(content)

            if (showPosition == true) {
                item.setPosition("${index + 1}")
            }
            addView(item)
        }
    }
}