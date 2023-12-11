/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManagerImpl
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManagerImpl
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManagerImpl
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManagerImpl
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepository
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepositoryImpl
import nl.rijksoverheid.ctr.appconfig.usecases.DeleteConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.DeleteConfigUseCaseImpl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Configure app config dependencies
 *
 * @param path Path for the config api, for example "holder" to fetch the config from <baseurl>/holder/config
 * @param versionCode version code
 */
fun appConfigModule(cdnUrl: String, path: String, versionCode: Int) = module {
    factory<ConfigRepository> { ConfigRepositoryImpl(get()) }
    factory<AppConfigPersistenceManager> { AppConfigPersistenceManagerImpl(get()) }
    factory<AppUpdatePersistenceManager> { AppUpdatePersistenceManagerImpl(get()) }
    factory<AppConfigStorageManager> { AppConfigStorageManagerImpl(androidContext().filesDir.path) }
    factory<DeleteConfigUseCase> {
        DeleteConfigUseCaseImpl(
            androidContext().filesDir.path
        )
    }
    factory<RecommendedUpdatePersistenceManager> { RecommendedUpdatePersistenceManagerImpl(get()) }

    single {
        val okHttpClient = get<OkHttpClient>(OkHttpClient::class).newBuilder().build()
        val retrofit = get<Retrofit>(Retrofit::class)
        val baseUrl = cdnUrl.toHttpUrl().newBuilder().addPathSegments("$path/").build()
        retrofit.newBuilder().baseUrl(baseUrl).client(okHttpClient).build()
            .create(AppConfigApi::class.java)
    }

    viewModel<AppConfigViewModel> {
        AppConfigViewModelImpl(
            get()
        )
    }
}
