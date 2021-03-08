package nl.rijksoverheid.ctr.verifier

import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.AppStatusStringProvider
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.LoadPublicKeysUseCase
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_policy.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MainApplication : SharedApplication(), CoronaCheckApp, AppStatusStringProvider {

    private val loadPublicKeysUseCase: LoadPublicKeysUseCase by inject()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(
                mainModule,
                sharedModule,
                appConfigModule("verifier", BuildConfig.VERSION_CODE),
                introductionModule,
                apiModule(nl.rijksoverheid.ctr.api.BuildConfig.BASE_API_URL)
            )
        }

        // If we have public keys stored, load them so they can be used by CTCL
        cachedAppConfigUseCase.getCachedPublicKeys()?.let {
            loadPublicKeysUseCase.load(it)
        }
    }

    override fun getIntroductionData(): CoronaCheckApp.IntroductionData {
        return CoronaCheckApp.IntroductionData(
            onboardingItems = listOf(
                OnboardingItem(
                    R.drawable.illustration_onboarding_1,
                    R.string.onboarding_screen_1_title,
                    R.string.onboarding_screen_1_description
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_2,
                    R.string.onboarding_screen_2_title,
                    R.string.onboarding_screen_2_description
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_3,
                    R.string.onboarding_screen_3_title,
                    R.string.onboarding_screen_3_description
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_4,
                    R.string.onboarding_screen_4_title,
                    R.string.onboarding_screen_4_description
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
            launchScreen = R.drawable.launch_screen,
            appSetupTextResource = R.string.app_setup_text
        )
    }

    override fun getAppStatusStrings(): AppStatusStringProvider.AppStatusStrings {
        return AppStatusStringProvider.AppStatusStrings(
            appStatusDeactivatedTitle = R.string.app_status_deactivated_title,
            appStatusDeactivatedMessage = R.string.app_status_deactivated_message,
            appStatusDeactivatedAction = R.string.app_status_deactivated_action,
            appStatusUpdateRequiredAction = R.string.app_status_update_required_action,
            appStatusUpdateRequiredMessage = R.string.app_status_update_required_message,
            appStatusUpdateRequiredTitle = R.string.app_status_update_required_title,
            appStatusInternetRequiredTitle = R.string.app_status_internet_required_title,
            appStatusInternetRequiredMessage = R.string.app_status_internet_required_message,
            appStatusInternetRequiredAction = R.string.app_status_internet_required_action
        )
    }
}
