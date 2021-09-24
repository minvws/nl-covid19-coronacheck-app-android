package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
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

    private val animationView: LottieAnimationView

    private val background: ImageView

    init {
        val view = inflate(context, R.layout.widget_qr_code_animation, this)
        animationView = view.findViewById(R.id.animation_view)
        background = view.findViewById(R.id.image)
        setOnClickListener {
            animationView.run {
                scaleX = if (scaleX == 1F) -1F else 1F
                playAnimation()
            }
        }
    }

    fun setWidget(@RawRes animation: Int) {
        animationView.setAnimation(animation)
        animationView.playAnimation()
    }
}
