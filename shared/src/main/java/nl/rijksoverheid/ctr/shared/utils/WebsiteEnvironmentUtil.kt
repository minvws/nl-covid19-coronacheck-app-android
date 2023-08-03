package nl.rijksoverheid.ctr.shared.utils

import android.content.Context
import nl.rijksoverheid.ctr.shared.models.Environment

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface WebsiteEnvironmentUtil {
    fun adjust(url: String): String
}

class WebsiteEnvironmentUtilImpl(
    private val context: Context
) : WebsiteEnvironmentUtil {

    companion object {
        const val wwwCoronaCheckUrl = "www.coronacheck.nl/"
        const val coronaCheckUrl = "coronacheck.nl/"
    }

    private fun stripWwwFromCoronacheckLinks(url: String): String {
        return url.replace(wwwCoronaCheckUrl, coronaCheckUrl)
    }

    override fun adjust(url: String): String {
        return when (Environment.get(context)) {
            Environment.Acc -> stripWwwFromCoronacheckLinks(url).replace(
                coronaCheckUrl,
                "web.acc.coronacheck.nl/"
            )
            else -> url
        }
    }
}
