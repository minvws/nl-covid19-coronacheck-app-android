package nl.rijksoverheid.ctr.introduction.ui.privacy_consent

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.WidgetScrollViewCheckboxButtonBinding


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
                    getResourceId(R.styleable.ScrollViewCheckboxButtonWidget_attachToScrollView, -1)
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
