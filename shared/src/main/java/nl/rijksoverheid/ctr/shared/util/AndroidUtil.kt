package nl.rijksoverheid.ctr.shared.util

import android.content.Context

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AndroidUtil {
    fun isSmallScreen(): Boolean
}

class AndroidUtilImpl(private val context: Context) : AndroidUtil {
    override fun isSmallScreen(): Boolean {
        return context.resources.displayMetrics.heightPixels <= 800
    }
}
