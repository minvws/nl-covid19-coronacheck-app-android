/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.os.Build
import java.util.*

fun Context.locale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    resources.configuration.locales[0]
} else {
    resources.configuration.locale
}

fun Context.getString(resourceName: String): String {
    val packageName: String = packageName
    val resId: Int = resources.getIdentifier(resourceName, "string", packageName)
    if (resId == 0) {
        return ""
    }
    return getString(resId)
}