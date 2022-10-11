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
import java.util.Locale

fun Context.locale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    resources.configuration.locales[0]
} else {
    @Suppress("DEPRECATION")
    resources.configuration.locale
}
