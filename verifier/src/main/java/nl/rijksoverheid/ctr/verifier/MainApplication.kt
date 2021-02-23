package nl.rijksoverheid.ctr.verifier

import android.content.Intent
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.api.apiModule
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.IntroductionActivity
import nl.rijksoverheid.ctr.introduction.introductionModule
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_policy.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.StatusFragmentDirections
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MainApplication : SharedApplication(), CoronaCheckApp {

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
                val intent = Intent(it, VerifierMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                it.startActivity(intent)
                it.overridePendingTransition(0, 0)
            },
            skipIntroductionCallback = {
                it.findNavController().navigate(StatusFragmentDirections.actionScanQr())
            },
            launchIntroductionCallback = {
                val intent = Intent(it, IntroductionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                it.startActivity(intent)
                it.overridePendingTransition(0, 0)
            }
        )
    }
}
