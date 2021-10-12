package nl.rijksoverheid.ctr.design.ext

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.accessibility.AccessibilityManager
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import nl.rijksoverheid.ctr.design.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun Context.getAppThemeWindowBackgroundColor(): Int {
    val theme = ContextThemeWrapper(this, R.style.AppTheme).theme
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
    return typedValue.data
}

fun Context.isScreenReaderOn(): Boolean {
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (am.isEnabled) {
        val serviceInfoList =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        if (serviceInfoList.isNotEmpty())
            return true
    }
    return false
}

fun Context.getThemeColorStateList(@AttrRes attribute: Int): ColorStateList = TypedValue().let {
    theme.resolveAttribute(
        attribute,
        it,
        true
    ); AppCompatResources.getColorStateList(this, it.resourceId)
}

fun Context.getAttrColor(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}
