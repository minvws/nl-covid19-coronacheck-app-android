package nl.rijksoverheid.ctr.holder

import android.util.Log
import androidx.work.Configuration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.*
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.holder.modules.*
import nl.rijksoverheid.ctr.holder.persistence.HolderWorkerFactory
import nl.rijksoverheid.ctr.holder.persistence.WorkerManagerWrapper
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManager
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.introduction.introductionModule
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

    private val secretKeyUseCase: SecretKeyUseCase by inject()
    private val holderDatabase: HolderDatabase by inject()
    private val testResultsMigrationManager: TestResultsMigrationManager by inject()
    private val holderWorkerFactory: HolderWorkerFactory by inject()
    private val appConfigStorageManager: AppConfigStorageManager by inject()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val workerManagerWrapper: WorkerManagerWrapper by inject()


    private val holderModules = listOf(
        storageModule,
        greenCardUseCasesModule,
        eventsUseCasesModule,
        secretUseCasesModule,
        testProvidersUseCasesModule,
        utilsModule,
        viewModels,
        cardUtilsModule,
        repositoriesModule,
        qrsModule,
        appModule,
        errorsModule,
        retrofitModule(BuildConfig.BASE_API_URL),
        responsesModule,
    ).toTypedArray()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HolderApplication)
            modules(
                *holderModules,
                holderIntroductionModule,
                apiModule(
                    BuildConfig.BASE_API_URL.toHttpUrl(),
                    BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH,
                    BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS,
                    BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS,
                    BuildConfig.CERTIFICATE_PINS,
                ),
                sharedModule,
                appConfigModule(BuildConfig.CDN_API_URL,"holder", BuildConfig.VERSION_CODE),
                introductionModule,
                *getAdditionalModules().toTypedArray(),
                designModule
            )
        }

        // Generate and store secret key to be used by rest of the app
        secretKeyUseCase.persist()

        // cancel pending refresh credentials jobs scheduled from app version 2.1.7
        workerManagerWrapper.cancel(this)

        // Create default wallet in database if empty
        GlobalScope.launch {
            if (holderDatabase.walletDao().getAll().isEmpty()) {
                holderDatabase.walletDao().insert(
                    WalletEntity(
                        id = 1,
                        label = "main"
                    )
                )
            }

            testResultsMigrationManager.removeOldCredential()
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
