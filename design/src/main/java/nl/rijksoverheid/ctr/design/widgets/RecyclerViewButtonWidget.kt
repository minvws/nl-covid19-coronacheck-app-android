package nl.rijksoverheid.ctr.design.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.WidgetRecyclerViewButtonBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class RecyclerViewButtonWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: WidgetRecyclerViewButtonBinding

    private var attachToRecyclerViewId: Int? = null
    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    init {
        elevation = 0f
        WidgetRecyclerViewButtonBinding.inflate(LayoutInflater.from(context), this)
        binding = WidgetRecyclerViewButtonBinding.bind(this)
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RecyclerViewButtonWidget,
            0, 0
        ).apply {
            try {
                attachToRecyclerViewId =
                    getResourceId(R.styleable.RecyclerViewButtonWidget_attachToRecyclerView, -1)
            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        attachToRecyclerViewId?.let {
            val parentLayout = parent as ViewGroup
            val recyclerView = parentLayout.findViewById<RecyclerView>(it)
            preDrawListener = ViewTreeObserver.OnPreDrawListener {
                cardElevation = if (recyclerView?.canScrollVertically(1) == true) {
                    resources.getDimensionPixelSize(R.dimen.scroll_view_button_elevation)
                        .toFloat()
                } else {
                    0f
                }
                true
            }
            recyclerView.viewTreeObserver.addOnPreDrawListener(preDrawListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        attachToRecyclerViewId?.let {
            val parentLayout = parent as ViewGroup
            val recyclerView = parentLayout.findViewById<RecyclerView>(it)
            recyclerView.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
        }
    }

    fun showError() {
        binding.errorContainer.isVisible = true
    }

    fun hideError() {
        binding.errorContainer.isVisible = false
    }

    fun setButtonClick(onClick: () -> Unit) {
        binding.button.setOnClickListener {
            onClick.invoke()
        }
    }
}
