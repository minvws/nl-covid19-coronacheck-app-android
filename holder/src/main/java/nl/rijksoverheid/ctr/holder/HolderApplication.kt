package nl.rijksoverheid.ctr.holder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.holder.modules.appModule
import nl.rijksoverheid.ctr.holder.modules.errorsModule
import nl.rijksoverheid.ctr.holder.modules.holderAppStatusModule
import nl.rijksoverheid.ctr.holder.modules.holderPreferenceModule
import nl.rijksoverheid.ctr.holder.modules.repositoriesModule
import nl.rijksoverheid.ctr.holder.modules.storageModule
import nl.rijksoverheid.ctr.holder.modules.utilsModule
import nl.rijksoverheid.ctr.holder.modules.viewModels
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class HolderApplication : SharedApplication() {

    private val holderDatabase: HolderDatabase by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    open fun coroutineScopeBlock(block: suspend () -> Unit) {
        coroutineScope.launch { block() }
    }

    private val holderModules = listOf(
        storageModule,
        utilsModule,
        viewModels,
        repositoriesModule,
        appModule,
        errorsModule(BuildConfig.FLAVOR),
    ).toTypedArray()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HolderApplication)
            modules(
                *holderModules,
                holderAppStatusModule,
                apiModule(
                    BuildConfig.BASE_API_URL.toHttpUrl(),
                    BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH,
                    BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS,
                    BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS
                ),
                sharedModule,
                appConfigModule(BuildConfig.CDN_API_URL, "holder", BuildConfig.VERSION_CODE),
                *getAdditionalModules().toTypedArray(),
                designModule
            )
        }

        coroutineScopeBlock {
            HolderDatabase.deleteDatabase(this)
        }
    }

    override fun getAdditionalModules(): List<Module> {
        return listOf(holderPreferenceModule)
    }
}
