/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import java.util.Locale

fun Context.locale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    resources.configuration.locales[0]
} else {
    @Suppress("DEPRECATION")
    resources.configuration.locale
}

fun Context.getDisplaySize(): Size {
    val displayManager =
        getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    val primaryDisplay = displayManager.displays.firstOrNull()
    return if (primaryDisplay == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        @Suppress("DEPRECATION")
        val display =
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        DisplayMetrics().also {
            @Suppress("DEPRECATION")
            display.getRealMetrics(it)
        }.let { Size(it.widthPixels, it.heightPixels) }
    } else {
        val windowContext = createWindowContext(
            primaryDisplay,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            null
        )
        val windowManager = windowContext.getSystemService(WindowManager::class.java)
        val bounds = windowManager.currentWindowMetrics.bounds
        Size(bounds.width(), bounds.height())
    }
}
