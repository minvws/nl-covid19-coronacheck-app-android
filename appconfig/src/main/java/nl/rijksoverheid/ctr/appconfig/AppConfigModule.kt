/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.usecase.AppConfigUseCase
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
    factory { AppConfigUseCase(get(), get()) }
    factory<AppConfigPersistenceManager> { AppConfigPersistenceManagerImpl(get()) }
    factory<CachedAppConfigUseCase> { CachedAppConfigUseCaseImpl(get(), get()) }

    single {
        val retrofit = get(Retrofit::class.java)
        val baseUrl = retrofit.baseUrl().newBuilder().addPathSegments("$path/").build()
        retrofit.newBuilder().baseUrl(baseUrl).build().create(AppConfigApi::class.java)
    }

    viewModel {
        AppStatusViewModel(get(), versionCode)
    }
}
