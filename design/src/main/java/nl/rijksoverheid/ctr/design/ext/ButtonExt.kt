@file:Suppress("DEPRECATION")

package nl.rijksoverheid.ctr.design.ext

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Button.setButtonColor(@ColorRes color: Int) {
    val color = ContextCompat.getColor(this.context, color)
    if (Build.VERSION.SDK_INT >= 29)
        this.background.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
    else
        this.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
}