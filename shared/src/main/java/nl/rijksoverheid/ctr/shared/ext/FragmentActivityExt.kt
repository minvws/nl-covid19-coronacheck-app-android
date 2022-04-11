package nl.rijksoverheid.ctr.shared.ext

import android.animation.ObjectAnimator
import android.os.Build
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.core.animation.doOnEnd
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun FragmentActivity.findNavControllerSafety(currentId: Int): NavController? {
    return try {
        findNavController(currentId)
    } catch (e: Exception) {
        null
    }
}

fun FragmentActivity.disableSplashscreenExitAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        /**
         * by default the splashscreen fades out the icon and
         * we don't want that in order to transit nicely
         * to [nl.rijksoverheid.ctr.introduction.setup.SetupFragment]
         */
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val anim = ObjectAnimator.ofFloat(
                splashScreenView,
                View.ALPHA,
                0.99f,
            )
            anim.duration = 100
            anim.doOnEnd {
                splashScreenView.remove()
                // the new splashscreen resets the system bar color (in SplashScreenView#restoreSystemUIColors, so have to set it again
                window.insetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
            }
            anim.start()
        }
    }
}
