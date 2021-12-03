package nl.rijksoverheid.ctr.design.ext

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import nl.rijksoverheid.ctr.design.spans.LinkTransformationMethod


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun TextView.enableHtmlLinks() {
    this.transformationMethod = LinkTransformationMethod(method = LinkTransformationMethod.Method.WebLinks)
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.enableCustomLinks(onLinkClick: () -> Unit) {
    this.transformationMethod = LinkTransformationMethod(method = LinkTransformationMethod.Method.CustomLinks(onLinkClick))
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.tintDrawable(@ColorRes color: Int) {
    for (drawable in compoundDrawablesRelative) {
        if (drawable != null) {
            drawable.colorFilter =
                PorterDuffColorFilter(
                    ContextCompat.getColor(context, color),
                    PorterDuff.Mode.SRC_IN
                )
        }
    }
}