package nl.rijksoverheid.ctr.verifier

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.introduction.IntroductionFragment
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.IntentUtil
import nl.rijksoverheid.ctr.verifier.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject
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
    private val appConfigViewModel: AppConfigViewModel by viewModel()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val dialogUtil: DialogUtil by inject()
    private val intentUtil: IntentUtil by inject()

    private var isFreshStart: Boolean = true // track if this is a fresh start of the app

    var returnUri: String? = null // return uri to external app given as argument from deeplink
    private var hasHandledDeeplink: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setProductionFlags()

        observeStatuses()
    }

    override fun onStart() {
        super.onStart()
        if (isIntroductionFinished()) {
            if (isFreshStart) {
                // Force retrieval of config once on startup for clock deviation checks
                appConfigViewModel.refresh(mobileCoreWrapper, force = true)
            } else {
                // Only get app config on every app foreground when introduction is finished and the app has already started
                appConfigViewModel.refresh(mobileCoreWrapper)
            }
            isFreshStart = false
        }
    }

    private fun observeStatuses() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        introductionViewModel.introductionStatusLiveData.observe(this, EventObserver {
            navController.navigate(R.id.action_introduction, IntroductionFragment.getBundle(it))
        })

        appConfigViewModel.appStatusLiveData.observe(this, {
            handleAppStatus(it, navController)
        })

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            if (destination.id == R.id.nav_main) {
                // Persist deeplink return uri in case it's not used immediately because of onboarding
                arguments?.getString("returnUri")?.let { returnUri = it }
                navigateDeeplink(navController)
            }

            // verifier can stay active for a long time, so it is not sufficient
            // to try to refresh the config only every time the app resumes.
            // We do track if the app was recently (re)started to avoid double config calls
            if (!isFreshStart && isIntroductionFinished()) {
                appConfigViewModel.refresh(mobileCoreWrapper)
            } else {
                isFreshStart = false
            }
        }
    }

    private fun navigateDeeplink(navController: NavController) {
        if (returnUri != null && !hasHandledDeeplink && isIntroductionFinished()) {
            navController.navigate(RootNavDirections.actionScanner())
        }
        hasHandledDeeplink = true
    }

    private fun isIntroductionFinished() =
        introductionViewModel.getIntroductionStatus() is IntroductionStatus.IntroductionFinished

    private fun handleAppStatus(
        appStatus: AppStatus,
        navController: NavController
    ) {
        if (appStatus is AppStatus.UpdateRecommended) {
            showRecommendedUpdateDialog()
            return
        }

        if (appStatus !is AppStatus.NoActionRequired) navigateToAppStatus(appStatus, navController)
    }

    private fun navigateToAppStatus(
        appStatus: AppStatus,
        navController: NavController
    ) {
        val bundle = bundleOf(AppStatusFragment.EXTRA_APP_STATUS to appStatus)
        navController.navigate(R.id.action_app_status, bundle)
    }

    private fun showRecommendedUpdateDialog() {
        dialogUtil.presentDialog(
            context = this,
            title = R.string.app_status_update_recommended_title,
            message = getString(R.string.app_status_update_recommended_message),
            positiveButtonText = R.string.app_status_update_recommended_action,
            positiveButtonCallback = { intentUtil.openPlayStore(this) },
            negativeButtonText = R.string.app_status_update_recommended_dismiss_action
        )
    }

    private fun setProductionFlags() {
        if (BuildConfig.FLAVOR == "prod") {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}
