package nl.rijksoverheid.ctr.shared

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.repositories.ConfigRepository
import nl.rijksoverheid.ctr.shared.repositories.EventRepository
import nl.rijksoverheid.ctr.shared.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.util.QrCodeUtils
import nl.rijksoverheid.ctr.shared.util.ZxingQrCodeUtils
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val sharedModule = module {

    single {
        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()

        val retroFit = Retrofit.Builder()
            .baseUrl("https://api-ct.bananenhalen.nl")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
        retroFit.create(TestApiClient::class.java)
    }
    single { Moshi.Builder().build() }
    single<QrCodeUtils> { ZxingQrCodeUtils() }

    // Utils
    single { CryptoUtil() }

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
    single { EventRepository(get()) }
    single { ConfigRepository(get()) }
}
