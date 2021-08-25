package nl.rijksoverheid.ctr.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.CacheOverrideInterceptor
import nl.rijksoverheid.ctr.api.interceptors.SignedResponseInterceptor
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.json.JsonObjectJsonAdapter
import nl.rijksoverheid.ctr.api.json.LocalDateJsonAdapter
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun apiModule(
    baseUrl: String,
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
            }
            .addInterceptor(
                SignedResponseInterceptor(
                    signatureCertificateCnMatch = signatureCertificateCnMatch,
                    testProviderApiChecks = testProviderApiChecks
                )
            ).build()
    }

    single {
        val okHttpClient = get<OkHttpClient>(OkHttpClient::class)
            .newBuilder()
            .apply {
                if (coronaCheckApiChecks) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .addTrustedCertificate(EV_ROOT_CA.decodeCertificatePem())
                        .build()
                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
                if (!BuildConfig.DEBUG) {
                    connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
                }
            }
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single {
        Moshi.Builder()
            .add(Base64JsonAdapter())
            .add(JsonObjectJsonAdapter())
            .add(OffsetDateTimeJsonAdapter())
            .add(LocalDateJsonAdapter())
    }
}
