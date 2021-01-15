/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.network

import android.content.Context
import android.net.Uri
import nl.rijksoverheid.ctr.BuildConfig
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

private var okHttpClient: OkHttpClient? = null

internal fun createOkHttpClient(context: Context): OkHttpClient {
    return okHttpClient ?: OkHttpClient.Builder()
        // enable cache for config and resource bundles
        .cache(Cache(File(context.cacheDir, "http"), 32 * 1024 * 1024))
        .callTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (Timber.forest().isNotEmpty()) {
                addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Timber.tag("OkHttpClient").d(message)
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            addInterceptor(UserAgentInterceptor(context))
            // Commented out due to no certificates being in place yet
//            if (BuildConfig.FEATURE_SSL_PINNING) {
//                connectionSpecs(
//                    listOf(
//                        ConnectionSpec.MODERN_TLS
//                    )
//                )
//                certificatePinner(
//                    CertificatePinner.Builder()
//                        .add(Uri.parse(BuildConfig.BASE_API_URL).host!!, Obfuscator.deObfuscate(BuildConfig.OBFUSCATED_SSL_PIN))
//                        .build()
//                )
//            }
        }.build().also { okHttpClient = it }
}