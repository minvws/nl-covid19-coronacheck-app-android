package nl.rijksoverheid.ctr.verifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.introduction.IntroductionFragment
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.verifier.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierMainActivity : AppCompatActivity() {

    private val introductionViewModel: IntroductionViewModel by viewModel()
    private val appStatusViewModel: AppConfigViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (!introductionViewModel.introductionFinished()) {
            navController.navigate(
                R.id.action_introduction,
                IntroductionFragment.getBundle(
                    introductionData = IntroductionData(
                        onboardingItems = listOf(
                            OnboardingItem(
                                R.drawable.illustration_onboarding_1,
                                R.string.onboarding_screen_1_title,
                                R.string.onboarding_screen_1_description
                            ),
                            OnboardingItem(
                                R.drawable.illustration_onboarding_2,
                                R.string.onboarding_screen_2_title,
                                R.string.onboarding_screen_2_description,
                                true
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
                        )
                    )
                )
            )
        }

        appStatusViewModel.appStatusLiveData.observe(this, {
            if (it !is AppStatus.NoActionRequired) {
                val bundle = bundleOf(AppStatusFragment.EXTRA_APP_STATUS to it)
                navController.navigate(R.id.action_app_status, bundle)
            }
        })
    }

    fun launchScannerFlow() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(
            R.id.action_nav_scanner, QrCodeScannerFragment.getBundle(
                title = getString(
                    R.string.scanner_custom_title
                ),
                message = getString(
                    R.string.scanner_custom_message
                ),
                rationaleDialog = QrCodeScannerFragment.RationaleDialog(
                    title = getString(R.string.camera_rationale_dialog_title),
                    description = getString(R.string.camera_rationale_dialog_description),
                    okayButtonText = getString(R.string.ok)
                )
            )
        )
    }

    override fun onStart() {
        super.onStart()
        // Only get app config on every app foreground when introduction is finished
        if (introductionViewModel.introductionFinished()) {
            appStatusViewModel.refresh()
        }
    }

}
