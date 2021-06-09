@file:Suppress("DEPRECATION")

package nl.rijksoverheid.ctr.design.ext

import android.widget.Button
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import nl.rijksoverheid.ctr.design.R

fun Button.setButtonColor(@ColorRes color: Int) {
    this.backgroundTintList = ContextCompat.getColorStateList(this.context, color)
}
