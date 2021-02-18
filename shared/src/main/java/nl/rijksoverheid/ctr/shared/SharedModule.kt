package nl.rijksoverheid.ctr.shared

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.shared.api.SignedResponseInterceptor
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.shared.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.shared.json.RemoteTestStatusJsonAdapter
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.ResponseError
import nl.rijksoverheid.ctr.shared.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.repositories.ConfigRepository
import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase
import nl.rijksoverheid.ctr.shared.util.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
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

    single {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .addNetworkInterceptor(SignedResponseInterceptor())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor {
                        Timber.tag("OkHttp").d(it)
                    }.setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
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

    single {
        AppStatusUseCase(get())
    }

    // Repositories
    single { ConfigRepository(get()) }
    single { TestResultRepository() }
}
