/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.network

import android.content.Context
import android.os.Build
import nl.rijksoverheid.ctr.BuildConfig
import nl.rijksoverheid.ctr.R
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that modifies the user-agent so it contains the Device model,
 * OS version and App version.
 */
class UserAgentInterceptor(val context: Context) : Interceptor {

    private val userAgent: String = "${context.getString(R.string.app_name)}/${BuildConfig.VERSION_CODE} (${Build.MANUFACTURER} ${Build.MODEL}) Android (${Build.VERSION.SDK_INT})"

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithUserAgent = chain.request().newBuilder()
            .header(USER_AGENT, userAgent)
            .build()

        return chain.proceed(requestWithUserAgent)
    }

    companion object {
        private const val USER_AGENT = "User-Agent"
    }

}