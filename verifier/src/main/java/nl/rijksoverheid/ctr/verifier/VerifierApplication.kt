package nl.rijksoverheid.ctr.verifier

import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.*
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.design.menu.about.AboutAppResourceProvider
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
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
open class VerifierApplication : SharedApplication(), CoronaCheckApp, AboutAppResourceProvider {

    private val loadPublicKeysUseCase: LoadPublicKeysUseCase by inject()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val appConfigUtil: AppConfigUtil by inject()
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

    override fun getSetupData(): CoronaCheckApp.SetupData {
        return CoronaCheckApp.SetupData(
            appSetupTextResource = R.string.app_setup_text
        )
    }

    override fun getOnboardingData(): CoronaCheckApp.OnboardingData {
        return CoronaCheckApp.OnboardingData(
            onboardingItems = listOf(
                OnboardingItem(
                    R.drawable.illustration_onboarding_1,
                    R.string.onboarding_screen_1_title,
                    appConfigUtil.getStringWithTestValidity(R.string.onboarding_screen_1_description)
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_2,
                    R.string.onboarding_screen_2_title,
                    appConfigUtil.getStringWithTestValidity(R.string.onboarding_screen_2_description)
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_3,
                    R.string.onboarding_screen_3_title,
                    getString(R.string.onboarding_screen_3_description)
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_4,
                    R.string.onboarding_screen_4_title,
                    getString(R.string.onboarding_screen_4_description)
                )
            ),
            privacyPolicyItems = listOf(
                PrivacyPolicyItem(
                    R.drawable.shield,
                    R.string.privacy_policy_1
                ),
                PrivacyPolicyItem(
                    R.drawable.shield,
                    R.string.privacy_policy_2
                ),
                PrivacyPolicyItem(
                    R.drawable.shield,
                    R.string.privacy_policy_3
                )
            ),
            introductionDoneCallback = {
                it.findNavController().navigate(R.id.action_scan_qr)
            },
            privacyPolicyStringResource = R.string.privacy_policy_description,
            privacyPolicyCheckboxStringResource = R.string.privacy_policy_checkbox_text,
            onboardingNextButtonStringResource = R.string.onboarding_next,
            backButtonStringResource = R.string.back,
            onboardingPageIndicatorStringResource = R.string.onboarding_page_indicator_label
        )
    }

    override fun getAboutThisAppData(): AboutAppResourceProvider.AboutData {
        return AboutAppResourceProvider.AboutData(
            aboutThisAppTextResource = R.string.about_this_app_description,
            appVersionTextResource = R.string.app_version,
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE.toString()
        )
    }
}
