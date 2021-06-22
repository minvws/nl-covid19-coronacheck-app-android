package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.ButtonWithIndicatorBinding
import nl.rijksoverheid.ctr.design.ext.setEnabledButtonColor

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
/**
 * A button that automatically adds a top elevation if given scrollview is scrollable
 */
class ButtonWithIndicatorLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    FrameLayout(context, attrs, defStyleAttr) {

    private var buttonText = ""

    init {
        ButtonWithIndicatorBinding.bind(View.inflate(context, R.layout.button_with_indicator, this))

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ButtonWithIndicatorLayout,
            0, 0
        ).apply {
            try {
                getText(R.styleable.ButtonWithIndicatorLayout_text)?.toString()?.let(this@ButtonWithIndicatorLayout::setButtonText)
            } finally {
                recycle()
            }
        }
    }

    private fun setButtonText(text: String) {
        val binding = ButtonWithIndicatorBinding.bind(this)
        binding.button.text = text
        buttonText = text
    }

    fun loading() {
        val binding = ButtonWithIndicatorBinding.bind(this)
        binding.button.isEnabled = false
        binding.button.text = ""
        binding.loading.visibility = VISIBLE
    }

    fun idle() {
        val binding = ButtonWithIndicatorBinding.bind(this)
        binding.loading.visibility = GONE
        binding.button.text = buttonText
        binding.button.isEnabled = true
    }

    fun setEnabledButtonColor(@ColorRes color: Int) {
        val binding = ButtonWithIndicatorBinding.bind(this)
        binding.button.setEnabledButtonColor(color)
    }

    fun setButtonOnClickListener(listener: OnClickListener) {
        val binding = ButtonWithIndicatorBinding.bind(this)
        binding.button.setOnClickListener(listener)
    }

    fun isButtonEnabled(isEnabled: Boolean) {
        val binding = ButtonWithIndicatorBinding.bind(this)
        binding.button.isEnabled = isEnabled
    }
}

