package nl.rijksoverheid.ctr.design.widgets

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.ButtonWithProgressWidgetBinding
import nl.rijksoverheid.ctr.design.ext.setEnabledButtonColor

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
/**
 * A widget with a button and progress indicator, for loading state support
 */
class ButtonWithProgressWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    private var buttonText = ""

    private val binding: ButtonWithProgressWidgetBinding

    init {
        ButtonWithProgressWidgetBinding.inflate(LayoutInflater.from(context), this)
        binding = ButtonWithProgressWidgetBinding.bind(this)
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ButtonWithProgressWidget,
            0, 0
        ).apply {
            try {
                getText(R.styleable.ButtonWithProgressWidget_text)?.toString()?.let(this@ButtonWithProgressWidget::setButtonText)
            } finally {
                recycle()
            }
        }
    }

    fun setButtonText(text: String) {
        binding.button.text = text
        buttonText = text
    }

    fun loading() {
        binding.button.isEnabled = false
        binding.button.text = ""
        binding.loading.visibility = VISIBLE
    }

    fun idle(isEnabled: Boolean) {
        binding.loading.visibility = GONE
        binding.button.text = buttonText
        binding.button.isEnabled = isEnabled
    }

    fun setEnabledButtonColor(@ColorRes color: Int) {
        binding.button.setEnabledButtonColor(color)
    }

    fun setButtonOnClickListener(listener: OnClickListener) {
        binding.button.setOnClickListener(listener)
    }

    fun accessibility(buttonAdditionalText: String) {
        binding.button.contentDescription = "${binding.button.text} $buttonAdditionalText"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.button.stateDescription = buttonText
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.loading.stateDescription = binding.loading.contentDescription
        }
    }
}
