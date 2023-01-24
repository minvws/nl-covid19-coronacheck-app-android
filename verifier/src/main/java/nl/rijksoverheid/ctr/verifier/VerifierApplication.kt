package nl.rijksoverheid.ctr.verifier

import errorsModule
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.qrscanner.qrScannerModule
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import nl.rijksoverheid.ctr.verifier.modules.storageModule
import nl.rijksoverheid.ctr.verifier.modules.verifierAppStatusModule
import nl.rijksoverheid.ctr.verifier.modules.verifierIntroductionModule
import nl.rijksoverheid.ctr.verifier.modules.verifierMobileCoreModule
import nl.rijksoverheid.ctr.verifier.modules.verifierModule
import nl.rijksoverheid.ctr.verifier.modules.verifierPreferenceModule
import nl.rijksoverheid.ctr.verifier.persistance.usecase.RandomKeyUseCase
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
open class VerifierApplication : SharedApplication() {

    private val appConfigStorageManager: AppConfigStorageManager by inject()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val randomKeyUseCase: RandomKeyUseCase by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@VerifierApplication)
            modules(
                apiModule(
                    BuildConfig.BASE_API_URL.toHttpUrl(),
                    BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH,
                    BuildConfig.FEATURE_CORONA_CHECK_API_CHECKS,
                    BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS
                ),
                verifierModule(),
                verifierIntroductionModule,
                verifierAppStatusModule,
                sharedModule,
                appConfigModule(BuildConfig.CDN_API_URL, "verifier", BuildConfig.VERSION_CODE),
                introductionModule,
                *getAdditionalModules().toTypedArray(),
                designModule,
                qrScannerModule,
                errorsModule,
                storageModule
            )
        }

        if (appConfigStorageManager.areConfigFilesPresentInFilesFolder()) {
            mobileCoreWrapper.initializeVerifier(applicationContext.filesDir.path)
        }

        randomKeyUseCase.persist()
    }

    override fun getAdditionalModules(): List<Module> {
        return listOf(verifierPreferenceModule, verifierMobileCoreModule)
    }
}
