package nl.rijksoverheid.ctr.verifier.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.VerifierWidgetScrollViewButtonBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierScrollViewButtonWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: VerifierWidgetScrollViewButtonBinding

    private var attachToScrollViewId: Int? = null
    private var scrollViewGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        elevation = 0f
        VerifierWidgetScrollViewButtonBinding.inflate(LayoutInflater.from(context), this)
        binding = VerifierWidgetScrollViewButtonBinding.bind(this)
        binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.red))

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

    fun hideSecondaryButton() {
        binding.secondaryButton.isInvisible = true
    }

}
