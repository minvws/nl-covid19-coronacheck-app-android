package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.WidgetScrollViewButtonBinding

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
class ScrollViewButtonWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var attachToScrollViewId: Int? = null
    private var scrollViewGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        elevation = 0f
        WidgetScrollViewButtonBinding.inflate(LayoutInflater.from(context), this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScrollViewButtonWidget,
            0, 0
        ).apply {
            try {
                val buttonText = getText(R.styleable.ScrollViewButtonWidget_buttonText)
                buttonText?.let {
                    setButtonText(buttonText.toString())
                }

                setButtonEnabled(
                    getBoolean(
                        R.styleable.ScrollViewButtonWidget_buttonEnabled,
                        true
                    )
                )

                attachToScrollViewId =
                    getResourceId(R.styleable.ScrollViewButtonWidget_attachToScrollView, -1)
            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        attachToScrollViewId?.let {
            val parentLayout = parent as ViewGroup
            val scrollView = parentLayout.findViewById<ScrollView>(it)
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

        attachToScrollViewId?.let {
            val parentLayout = parent as ViewGroup
            val scrollView = parentLayout.findViewById<ScrollView>(it)
            scrollView.viewTreeObserver.removeOnGlobalLayoutListener(scrollViewGlobalLayoutListener)
        }
    }

    fun setButtonText(text: String) {
        val binding = WidgetScrollViewButtonBinding.bind(this)
        binding.button.text = text
    }

    fun setButtonClick(onClick: () -> Unit) {
        val binding = WidgetScrollViewButtonBinding.bind(this)
        binding.button.setOnClickListener {
            onClick.invoke()
        }
    }

    fun setButtonEnabled(isEnabled: Boolean) {
        val binding = WidgetScrollViewButtonBinding.bind(this)
        binding.button.isEnabled = isEnabled
    }

}
