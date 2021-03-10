package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RawRes
import com.airbnb.lottie.LottieAnimationView
import nl.rijksoverheid.ctr.holder.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeAnimationWidget(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    @RawRes
    private val leftToRightAnim = R.raw.bike_lr

    @RawRes
    private val rightToLeftAnim = R.raw.bike_rl

    @RawRes
    private var currentAnim = leftToRightAnim

    init {
        val view = inflate(context, R.layout.widget_qr_code_animation, this)
        val lottieAnimationView = view.findViewById<LottieAnimationView>(R.id.animation_view)
        setOnClickListener {
            when (currentAnim) {
                leftToRightAnim -> playAnimation(
                    view = lottieAnimationView,
                    anim = rightToLeftAnim
                )
                rightToLeftAnim -> playAnimation(
                    view = lottieAnimationView,
                    anim = leftToRightAnim
                )
            }
        }
    }

    private fun playAnimation(view: LottieAnimationView, @RawRes anim: Int) {
        view.setAnimation(anim)
        view.playAnimation()
        currentAnim = anim
    }

}
