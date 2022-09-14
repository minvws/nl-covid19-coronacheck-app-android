package nl.rijksoverheid.ctr.api

import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import com.appmattus.certificatetransparency.loglist.LogListDataSourceFactory
import com.squareup.moshi.Moshi
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager
import nl.rijksoverheid.ctr.api.interceptors.CacheOverrideInterceptor
import nl.rijksoverheid.ctr.api.interceptors.SignedResponseInterceptor
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.json.DisclosurePolicyJsonAdapter
import nl.rijksoverheid.ctr.api.json.JsonObjectJsonAdapter
import nl.rijksoverheid.ctr.api.json.LocalDateJsonAdapter
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.shared.models.Environment
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun apiModule(
    baseUrl: HttpUrl,
    signatureCertificateCnMatch: String,
    coronaCheckApiChecks: Boolean,
    testProviderApiChecks: Boolean
) = module(override = true) {
    single {
        OkHttpClient.Builder()
            .addNetworkInterceptor(CacheOverrideInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor {
                        Timber.tag("OkHttp").d(it)
                    }.setLevel(HttpLoggingInterceptor.Level.BODY))
                }
                if (coronaCheckApiChecks) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .addPlatformTrustedCertificates()
                        .build()
                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        transparentTrustManager(handshakeCertificates.trustManager)
                    )
                }
                if (!BuildConfig.DEBUG) {
                    connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
                }
            }
            .addInterceptor(
                SignedResponseInterceptor(
                    signatureCertificateCnMatch = signatureCertificateCnMatch,
                    testProviderApiChecks = testProviderApiChecks,
                    Environment.get(androidContext()) == Environment.Acc
                )
            ).build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single {
        Moshi.Builder()
            .add(Base64JsonAdapter())
            .add(JsonObjectJsonAdapter())
            .add(OffsetDateTimeJsonAdapter())
            .add(LocalDateJsonAdapter())
            .add(DisclosurePolicyJsonAdapter())
    }
}

private fun transparentTrustManager(trustManager: X509TrustManager) =
    certificateTransparencyTrustManager(trustManager) {
        if (BuildConfig.DEBUG) {
            setLogger(object : CTLogger {
                override fun log(host: String, result: VerificationResult) {
                    Timber.tag("certificate transparency")
                        .d("host: $host, verification result: $result")
                }
            })
        }

        setLogListService(LogListDataSourceFactory.createLogListService())
    }
