@file:Suppress("DEPRECATION")

package nl.rijksoverheid.ctr.design.ext

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import nl.rijksoverheid.ctr.design.R

/**
 * Sets only enabled state background color of the button
 */
fun Button.setEnabledButtonColor(@ColorRes color: Int) {
    this.backgroundTintList?.let { colorStateList ->
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this.context, color),
            colorStateList.getColorForState(intArrayOf(-android.R.attr.state_enabled), Color.BLACK)
        )

        this.backgroundTintList = ColorStateList(states, colors)
    }
}
