/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.utils

import android.content.Context

interface StringUtil {
    fun getStringFromResourceName(resourceName: String): String
}

class StringUtilImpl(private val context: Context) : StringUtil {
    override fun getStringFromResourceName(resourceName: String): String {
        val packageName: String = context.packageName
        val resId: Int = context.resources.getIdentifier(resourceName, "string", packageName)
        if (resId == 0) {
            return ""
        }
        return context.getString(resId)
    }
}
