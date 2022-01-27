package nl.rijksoverheid.ctr.verifier.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.WidgetScrollViewPolicyButtonBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScrollViewPolicyButtonWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: WidgetScrollViewPolicyButtonBinding

    private var attachToScrollViewId: Int = NO_ID
    private var scrollViewGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        elevation = 0f
        WidgetScrollViewPolicyButtonBinding.inflate(LayoutInflater.from(context), this)
        binding = WidgetScrollViewPolicyButtonBinding.bind(this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScrollViewPolicyButtonWidget,
            0, 0
        ).apply {
            try {
                attachToScrollViewId =
                    getResourceId(
                        R.styleable.ScrollViewPolicyButtonWidget_attachToScrollView,
                        NO_ID
                    )
            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (attachToScrollViewId > NO_ID) {
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

        if (attachToScrollViewId > NO_ID) {
            val parentLayout = parent as ViewGroup
            val scrollView = parentLayout.findViewById<ScrollView>(attachToScrollViewId)
            scrollView.viewTreeObserver.removeOnGlobalLayoutListener(scrollViewGlobalLayoutListener)
        }
    }

    fun setButtonClick(onClick: () -> Unit) {
        binding.button.setOnClickListener {
            onClick.invoke()
        }
    }

    fun hidePolicyIndication() {
        binding.indicationContainer.visibility = GONE
    }

    fun setPolicy(policy: VerificationPolicy) {
        binding.policyIndicator.backgroundTintList = ColorStateList.valueOf(
            context.getColor(
                when (policy) {
                    VerificationPolicy.VerificationPolicy1G -> R.color.primary_blue
                    VerificationPolicy.VerificationPolicy3G -> R.color.secondary_green
                }
            )
        )
        binding.policyIndicatorText.setHtmlText(context.getString(R.string.verifier_start_scan_qr_policy_indication, policy.configValue))
    }

    fun lock() {
        binding.button.visibility = GONE
    }

    fun unlock() {
        binding.button.visibility = VISIBLE
    }

}