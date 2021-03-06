/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApiCacheInterceptor
import nl.rijksoverheid.ctr.appconfig.usecase.*
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Configure app config dependencies
 *
 * @param path Path for the config api, for example "holder" to fetch the config from <baseurl>/holder/config
 * @param versionCode version code
 */
fun appConfigModule(path: String, versionCode: Int) = module {
    factory<ConfigRepository> { ConfigRepositoryImpl(get()) }
    factory<AppConfigUseCase> { AppConfigUseCaseImpl(get()) }
    factory<AppStatusUseCase> { AppStatusUseCaseImpl() }
    factory<AppConfigPersistenceManager> { AppConfigPersistenceManagerImpl(get()) }
    factory<CachedAppConfigUseCase> { CachedAppConfigUseCaseImpl(get(), get()) }
    factory { AppConfigApiCacheInterceptor(get()) }
    factory<PersistConfigUseCase> { PersistConfigUseCaseImpl(get(), get()) }


    single {
        val okHttpClient = get(OkHttpClient::class.java).newBuilder()
            .addInterceptor(AppConfigApiCacheInterceptor(get())).build()

        val retrofit = get(Retrofit::class.java)
        val baseUrl = retrofit.baseUrl().newBuilder().addPathSegments("$path/").build()
        retrofit.newBuilder().baseUrl(baseUrl).client(okHttpClient).build()
            .create(AppConfigApi::class.java)
    }

    viewModel {
        AppConfigViewModel(get(), get(), get(), versionCode)
    }
}
