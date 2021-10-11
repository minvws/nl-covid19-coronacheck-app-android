/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.items

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import nl.rijksoverheid.ctr.holder.databinding.ItemYourEventBinding
import nl.rijksoverheid.ctr.shared.utils.Accessibility.addAccessibilityAction
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityLabel
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton

class YourEventWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    private var onClickListener = {

    }

    val binding =
        ItemYourEventBinding.inflate(LayoutInflater.from(context), this, true)

    fun setContent(title: String, subtitle: String, infoClickListener: () -> Unit) {
        binding.rowTitle.text = title
        binding.rowSubtitle.setHtmlText(subtitle)

        with (binding.testResultsGroup) {
            onClickListener = {
                infoClickListener.invoke()
            }
            setOnClickListener { onClickListener.invoke() }

            setAccessibilityLabel(String.format("%s. %s.",
                binding.rowTitle.text,
                binding.rowSubtitle.text
            ))
            setAsAccessibilityButton(true)
            addAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_CLICK, binding.detailsButton.text)
        }
    }

    fun setButtonsEnabled(enabled: Boolean) {
        binding.detailsButton.isEnabled = enabled
        if (enabled) {
            binding.testResultsGroup.setOnClickListener { onClickListener.invoke() }
        } else {
            binding.testResultsGroup.setOnClickListener {  }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onClickListener = { }
    }
}
