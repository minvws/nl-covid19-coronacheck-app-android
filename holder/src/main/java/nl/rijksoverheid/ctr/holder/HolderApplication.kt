package nl.rijksoverheid.ctr.holder

import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.holder.dashboard.dashboardModule
import nl.rijksoverheid.ctr.holder.modules.appModule
import nl.rijksoverheid.ctr.holder.modules.cardUtilsModule
import nl.rijksoverheid.ctr.holder.modules.disclosurePolicyModule
import nl.rijksoverheid.ctr.holder.modules.errorsModule
import nl.rijksoverheid.ctr.holder.modules.eventsUseCasesModule
import nl.rijksoverheid.ctr.holder.modules.greenCardUseCasesModule
import nl.rijksoverheid.ctr.holder.modules.holderAppStatusModule
import nl.rijksoverheid.ctr.holder.modules.holderIntroductionModule
import nl.rijksoverheid.ctr.holder.modules.holderMobileCoreModule
import nl.rijksoverheid.ctr.holder.modules.holderPreferenceModule
import nl.rijksoverheid.ctr.holder.modules.qrsModule
import nl.rijksoverheid.ctr.holder.modules.repositoriesModule
import nl.rijksoverheid.ctr.holder.modules.responsesModule
import nl.rijksoverheid.ctr.holder.modules.retrofitModule
import nl.rijksoverheid.ctr.holder.modules.storageModule
import nl.rijksoverheid.ctr.holder.modules.testProvidersUseCasesModule
import nl.rijksoverheid.ctr.holder.modules.utilsModule
import nl.rijksoverheid.ctr.holder.modules.viewModels
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.qrscanner.qrScannerModule
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
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
open class HolderApplication : SharedApplication(), Configuration.Provider {

    private val holderDatabase: HolderDatabase by inject()
    private val holderWorkerFactory: WorkerFactory by inject()
    private val appConfigStorageManager: AppConfigStorageManager by inject()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    open fun coroutineScopeBlock(block: suspend () -> Unit) {
        coroutineScope.launch { block() }
    }

    private val holderModules = listOf(
        storageModule,
        greenCardUseCasesModule,
        eventsUseCasesModule,
        testProvidersUseCasesModule,
        utilsModule(BuildConfig.VERSION_CODE),
        viewModels,
        cardUtilsModule,
        repositoriesModule,
        qrsModule,
        appModule,
        errorsModule(BuildConfig.FLAVOR),
        retrofitModule(BuildConfig.BASE_API_URL, BuildConfig.CDN_API_URL),
        responsesModule,
        qrScannerModule,
        disclosurePolicyModule,
        dashboardModule
    ).toTypedArray()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HolderApplication)
            modules(
                *holderModules,
                holderIntroductionModule,
                holderAppStatusModule,
                apiModule(
                    BuildConfig.BASE_API_URL.toHttpUrl(),
                    BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH,
                    BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS,
                    BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS
                ),
                sharedModule,
                appConfigModule(BuildConfig.CDN_API_URL, "holder", BuildConfig.VERSION_CODE),
                introductionModule,
                *getAdditionalModules().toTypedArray(),
                designModule
            )
        }

        // Create default wallet in database if empty
        coroutineScopeBlock {
            if (holderDatabase.walletDao().getAll().isEmpty()) {
                holderDatabase.walletDao().insert(
                    WalletEntity(
                        id = 1,
                        label = "main"
                    )
                )
            }
        }

        if (appConfigStorageManager.areConfigFilesPresentInFilesFolder()) {
            mobileCoreWrapper.initializeHolder(applicationContext.filesDir.path)
        }
    }

    override fun getAdditionalModules(): List<Module> {
        return listOf(holderPreferenceModule, holderMobileCoreModule)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().apply {
            setMinimumLoggingLevel(if (BuildConfig.DEBUG) {
                Log.DEBUG
            } else {
                Log.ERROR
            })
            setWorkerFactory(holderWorkerFactory)
        }.build()
    }
}
