package nl.rijksoverheid.ctr.verifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import nl.rijksoverheid.ctr.appconfig.AppConfigUtil
import nl.rijksoverheid.ctr.introduction.IntroductionFragment
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.verifier.databinding.ActivityMainNewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NewVerifierMainActivity : AppCompatActivity() {

    private val appConfigUtil: AppConfigUtil by inject()
    private val introductionViewModel: IntroductionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (!introductionViewModel.introductionFinished()) {
            navController.navigate(
                R.id.nav_introduction,
                IntroductionFragment.getBundle(
                    introductionData = IntroductionData(
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
                        privacyPolicyStringResource = R.string.privacy_policy_description,
                        privacyPolicyCheckboxStringResource = R.string.privacy_policy_checkbox_text,
                        onboardingNextButtonStringResource = R.string.onboarding_next,
                        backButtonStringResource = R.string.back,
                        onboardingPageIndicatorStringResource = R.string.onboarding_page_indicator_label,
                        appSetupTextResource = R.string.app_setup_text
                    )
                )
            )
        }
    }

}
