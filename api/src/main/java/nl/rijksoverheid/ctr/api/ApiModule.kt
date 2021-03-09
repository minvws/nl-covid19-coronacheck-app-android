package nl.rijksoverheid.ctr.api

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.api.json.RemoteTestStatusJsonAdapter
import nl.rijksoverheid.ctr.api.models.RemoteTestResult
import nl.rijksoverheid.ctr.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.interceptors.CacheOverrideInterceptor
import nl.rijksoverheid.ctr.signing.certificates.DIGICERT_BTC_ROOT_CA
import nl.rijksoverheid.ctr.signing.certificates.EV_ROOT_CA
import nl.rijksoverheid.ctr.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.signing.certificates.ROOT_CA_G3
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun apiModule(baseUrl: String) = module(override = true) {
    // the base OkHttpClient for both API and test providers
    single {
        val context = get(Context::class.java)
        val cache = Cache(File(context.cacheDir, "http"), 10 * 1024 * 1024)

        OkHttpClient.Builder()
            .addNetworkInterceptor(CacheOverrideInterceptor())
            .addNetworkInterceptor(StethoInterceptor())
            .cache(cache)
            .followRedirects(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor {
                        Timber.tag("OkHttp").d(it)
                    }.setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .addInterceptor(SignedResponseInterceptor()).build()
    }

    single {
        val okHttpClient = get(OkHttpClient::class.java)
            .newBuilder()
            .apply {
                if (BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS) {
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
        val okHttpClient = get(OkHttpClient::class.java)
            .newBuilder()
            .apply {
                if (BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .addTrustedCertificate(ROOT_CA_G3.decodeCertificatePem())
                        .addTrustedCertificate(EV_ROOT_CA.decodeCertificatePem())
                        .addTrustedCertificate(PRIVATE_ROOT_CA.decodeCertificatePem())
                        .addTrustedCertificate(DIGICERT_BTC_ROOT_CA.decodeCertificatePem())
                        .build()

                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
            }.build()

        Retrofit.Builder()
            .client(okHttpClient)
            // required, although not used for TestProviders
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(TestProviderApiClient::class.java)
    }

    single<Converter<ResponseBody, SignedResponseWithModel<RemoteTestResult>>>(named("SignedResponseWithModel")) {
        get(Retrofit::class.java).responseBodyConverter(
            Types.newParameterizedType(
                SignedResponseWithModel::class.java,
                RemoteTestResult::class.java
            ), emptyArray()
        )
    }

    single<Converter<ResponseBody, nl.rijksoverheid.ctr.api.models.ResponseError>>(named("ResponseError")) {
        get(Retrofit::class.java).responseBodyConverter(
            nl.rijksoverheid.ctr.api.models.ResponseError::class.java, emptyArray()
        )
    }

    single {
        get(Retrofit::class).create(CoronaCheckApiClient::class.java)
    }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Base64JsonAdapter())
            .add(RemoteTestStatusJsonAdapter())
            .add(OffsetDateTimeJsonAdapter()).build()
    }
}
