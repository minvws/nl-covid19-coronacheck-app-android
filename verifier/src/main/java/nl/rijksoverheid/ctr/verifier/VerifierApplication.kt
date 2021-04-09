package nl.rijksoverheid.ctr.verifier

import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.*
import nl.rijksoverheid.ctr.appconfig.usecase.LoadPublicKeysUseCase
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.qrscanner.qrCodeScannerModule
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
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
open class VerifierApplication : SharedApplication() {

    private val loadPublicKeysUseCase: LoadPublicKeysUseCase by inject()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val sharedPreferenceMigration: SharedPreferenceMigration by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@VerifierApplication)
            modules(
                apiModule(
                    BuildConfig.BASE_API_URL,
                    BuildConfig.FLAVOR == "tst",
                    BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH,
                    BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS,
                    BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS
                ),
                verifierModule,
                sharedModule,
                appConfigModule("verifier", BuildConfig.VERSION_CODE),
                introductionModule,
                qrCodeScannerModule,
                *getAdditionalModules().toTypedArray(),
                designModule
            )
        }

        sharedPreferenceMigration.migrate()

        // If we have public keys stored, load them so they can be used by CTCL
        cachedAppConfigUseCase.getCachedPublicKeys()?.let {
            loadPublicKeysUseCase.load(it)
        }
    }

    override fun getAdditionalModules(): List<Module> {
        return listOf(verifierPreferenceModule)
    }
}
