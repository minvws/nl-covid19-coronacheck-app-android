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
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import nl.rijksoverheid.ctr.design.databinding.ViewIdentifierItemBinding

class AbbreviatedPersonalDetailsItemWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    val binding = ViewIdentifierItemBinding.inflate(LayoutInflater.from(context), this)

    fun setContent(content: String) {
        binding.itemText.text = content
    }

    fun setPosition(position: String) {
        binding.itemPosition.apply {
            visibility = View.VISIBLE
            text = position
        }
    }
}