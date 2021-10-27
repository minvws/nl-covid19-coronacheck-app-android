package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ScrollView
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
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

    private val binding: WidgetScrollViewButtonBinding

    private var attachToScrollViewId: Int? = null
    private var scrollViewGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        elevation = 0f
        WidgetScrollViewButtonBinding.inflate(LayoutInflater.from(context), this)
        binding = WidgetScrollViewButtonBinding.bind(this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScrollViewButtonWidget,
            0, 0
        ).apply {
            try {
                getText(R.styleable.ScrollViewButtonWidget_buttonText)?.let {
                    setButtonText(it.toString())
                }

                getText(R.styleable.ScrollViewButtonWidget_secondaryButtonText)?.let {
                    setSecondaryButtonText(it.toString())
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

    fun setIcon(@DrawableRes drawable: Int) {
        binding.button.run {
            setCompoundDrawablesWithIntrinsicBounds(
                null, null, ContextCompat.getDrawable(context, drawable), null
            )
            compoundDrawablePadding =
                resources.getDimensionPixelSize(R.dimen.deeplink_exit_icon_padding)
            setPadding(
                paddingLeft,
                resources.getDimensionPixelSize(R.dimen.deeplink_button_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.deeplink_button_padding_end),
                resources.getDimensionPixelSize(R.dimen.deeplink_button_padding_vertical),
            )
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
        binding.button.text = text
    }

    private fun setSecondaryButtonText(text: String) {
        binding.secondaryButton.text = text
        binding.secondaryButton.visibility = View.VISIBLE
    }

    fun setButtonClick(onClick: () -> Unit) {
        binding.button.setOnClickListener {
            onClick.invoke()
        }
    }

    fun setSecondaryButtonClick(onClick: () -> Unit) {
        binding.secondaryButton.setOnClickListener {
            onClick.invoke()
        }
    }

    fun setButtonEnabled(isEnabled: Boolean) {
        binding.button.isEnabled = isEnabled
    }

    fun customiseButton(block: (Button) -> Unit) {
        binding.button.run(block)
    }

    fun customiseSecondaryButton(block: (MaterialButton) -> Unit) {
        binding.secondaryButton.run(block)
    }

}
