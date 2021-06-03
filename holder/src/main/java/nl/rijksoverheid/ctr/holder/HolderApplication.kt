package nl.rijksoverheid.ctr.holder

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.*
import nl.rijksoverheid.ctr.appconfig.usecases.LoadPublicKeysUseCase
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.holder.modules.*
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.OriginDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManager
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import java.time.OffsetDateTime


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class HolderApplication : SharedApplication() {

    private val loadPublicKeysUseCase: LoadPublicKeysUseCase by inject()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val secretKeyUseCase: SecretKeyUseCase by inject()
    private val holderDatabase: HolderDatabase by inject()
    private val testResultsMigrationManager: TestResultsMigrationManager by inject()

    override fun onCreate() {
        super.onCreate()


        startKoin {
            androidContext(this@HolderApplication)
            modules(
                holderModule(BuildConfig.BASE_API_URL),
                holderIntroductionModule,
                apiModule(
                    BuildConfig.BASE_API_URL,
                    BuildConfig.FLAVOR == "tst",
                    BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH,
                    BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS,
                    BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS
                ),
                sharedModule,
                appConfigModule("holder", BuildConfig.VERSION_CODE),
                introductionModule,
                *getAdditionalModules().toTypedArray(),
                designModule
            )
        }

        // Generate and store secret key to be used by rest of the app
        secretKeyUseCase.persist()

        // If we have public keys stored, load them so they can be used by CTCL
        cachedAppConfigUseCase.getCachedPublicKeys()?.let {
            loadPublicKeysUseCase.load(it)
        }

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

            testResultsMigrationManager.migrateTestResults()
        }
    }

    override fun getAdditionalModules(): List<Module> {
        return listOf(holderPreferenceModule, holderMobileCoreModule)
    }
}
