package nl.rijksoverheid.ctr.design.ext

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.ContextThemeWrapper
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
