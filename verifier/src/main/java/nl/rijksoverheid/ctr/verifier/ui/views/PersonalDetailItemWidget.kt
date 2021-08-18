/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import nl.rijksoverheid.ctr.design.ext.resolveColorAttr
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.PersonalDetailsItemWidgetBinding

class PersonalDetailItemWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: PersonalDetailsItemWidgetBinding = PersonalDetailsItemWidgetBinding.bind(
        View.inflate(
            context,
            R.layout.personal_details_item_widget,
            this
        )
    )

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PersonalDetailsItemWidget,
            0, 0
        ).apply {
            try {
                getText(R.styleable.PersonalDetailsItemWidget_header)?.toString()
                    ?.let(this@PersonalDetailItemWidget::setHeader)
                getText(R.styleable.PersonalDetailsItemWidget_textContent)?.toString()
                    ?.let(this@PersonalDetailItemWidget::setContent)

            } finally {
                recycle()
            }
        }
    }

    fun setHeader(header: String) {
        binding.header.text = header
    }

    fun setContent(content: String) {
        binding.content.text = content
        if (content.contentEquals("_")) {
            setState(disabled = true)
        } else {
            setState(disabled = false)
        }
    }

    private fun setState(disabled: Boolean) {
        if (disabled) {
            binding.content.background = ContextCompat.getDrawable(
                context,
                R.drawable.shape_personal_detail_disabled_background
            )
            binding.header.setTextColor(ContextCompat.getColor(context, R.color.grey_5_halved))
        } else {
            binding.content.background =
                ContextCompat.getDrawable(context, R.drawable.shape_personal_detail_background)
            binding.header.setTextColor(context.resolveColorAttr(android.R.attr.textColorPrimary))
        }
    }


}