/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.WidgetScrollViewCheckboxButtonBinding


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScrollViewCheckboxButtonWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: WidgetScrollViewCheckboxButtonBinding

    private val attachToScrollViewId: Int
    private var scrollViewGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        elevation = 0f
        WidgetScrollViewCheckboxButtonBinding.inflate(LayoutInflater.from(context), this)
        binding = WidgetScrollViewCheckboxButtonBinding.bind(this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScrollViewCheckboxButtonWidget,
            0, 0
        ).apply {
            try {
                attachToScrollViewId =
                    getResourceId(
                        R.styleable.ScrollViewCheckboxButtonWidget_attachCheckboxToScrollView,
                        -1
                    )
                getText(R.styleable.ScrollViewCheckboxButtonWidget_checkboxText)?.toString()
                    ?.let { binding.checkbox.text = it }
                getText(R.styleable.ScrollViewCheckboxButtonWidget_checkboxErrorText)?.toString()
                    ?.let { binding.errorText.text = it }
                getText(R.styleable.ScrollViewCheckboxButtonWidget_checkboxButtonText)?.toString()
                    ?.let { binding.checkboxButton.text = it }
                binding.checkboxButton.isVisible =
                    !getBoolean(R.styleable.ScrollViewCheckboxButtonWidget_buttonHidden, false)
            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (attachToScrollViewId != View.NO_ID) {
            val parentLayout = parent as ViewGroup
            val scrollView = parentLayout.findViewById<ScrollView>(attachToScrollViewId)
            scrollViewGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                cardElevation = if (scrollView?.canScrollVertically(1) == true) {
                    resources.getDimensionPixelSize(R.dimen.scroll_view_button_elevation)
                        .toFloat()
                } else {
                    0f
                }
            }
            scrollView.viewTreeObserver.addOnGlobalLayoutListener(scrollViewGlobalLayoutListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (attachToScrollViewId != View.NO_ID) {
            val parentLayout = parent as ViewGroup
            val scrollView = parentLayout.findViewById<ScrollView>(attachToScrollViewId)
            scrollView.viewTreeObserver.removeOnGlobalLayoutListener(scrollViewGlobalLayoutListener)
        }
    }
}
