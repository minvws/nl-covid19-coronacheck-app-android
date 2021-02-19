package nl.rijksoverheid.ctr.shared

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.api.cache.CacheOverrideInterceptor
import nl.rijksoverheid.ctr.api.cachestrategy.CacheStrategyInterceptor
import nl.rijksoverheid.ctr.shared.api.SignedResponseInterceptor
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.api.TestProviderApiClient
import nl.rijksoverheid.ctr.shared.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.shared.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.shared.json.RemoteTestStatusJsonAdapter
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.ResponseError
import nl.rijksoverheid.ctr.shared.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.util.QrCodeScannerUtil
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import nl.rijksoverheid.ctr.shared.util.ZxingQrCodeScannerUtil
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
import java.time.Clock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

val sharedModule = module {

    single { Clock.systemDefaultZone() }

    // the base OkHttpClient for both API and test providers
    single {
        OkHttpClient.Builder()
            .addNetworkInterceptor(CacheOverrideInterceptor())
            .addNetworkInterceptor(StethoInterceptor())
            .addInterceptor(CacheStrategyInterceptor())
            .followRedirects(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor {
                        Timber.tag("OkHttp").d(it)
                    }.setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .addInterceptor(SignedResponseInterceptor())
    }

    single {
        val context = get(Context::class.java)
        val cache = Cache(File(context.cacheDir, "http"), 10 * 1024 * 1024)
        val okHttpClient = get(OkHttpClient.Builder::class.java)
            .cache(cache)
            .apply {
                if (BuildConfig.FEATURE_API_SSL_ROOT_CA) {
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
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single {
        val handshakeCertificates = HandshakeCertificates.Builder()
            .addTrustedCertificate(ROOT_CA_G3.decodeCertificatePem())
            .addTrustedCertificate(EV_ROOT_CA.decodeCertificatePem())
            .addTrustedCertificate(PRIVATE_ROOT_CA.decodeCertificatePem())
            .build()

        val okHttpClient = get(OkHttpClient.Builder::class.java)
            .apply {
                if (BuildConfig.FEATURE_TEST_PROVIDER_TRUSTED_ROOTS) {
                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
            }.build()

        Retrofit.Builder()
            .client(okHttpClient)
            // required, although not used for TestProviders
            .baseUrl(BuildConfig.BASE_API_URL)
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

    single<Converter<ResponseBody, ResponseError>>(named("ResponseError")) {
        get(Retrofit::class.java).responseBodyConverter(
            ResponseError::class.java, emptyArray()
        )
    }

    single {
        get(Retrofit::class).create(TestApiClient::class.java)
    }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Base64JsonAdapter())
            .add(RemoteTestStatusJsonAdapter())
            .add(OffsetDateTimeJsonAdapter()).build()
    }
    single<QrCodeScannerUtil> { ZxingQrCodeScannerUtil() }

    // Utils
    single { QrCodeUtil(get()) }
    single { CryptoUtil() }
    single { TestResultUtil(get()) }

    // Use cases
    single {
        SignatureValidUseCase(
            get(),
            get()
        )
    }

    single { TestResultRepository() }
}
