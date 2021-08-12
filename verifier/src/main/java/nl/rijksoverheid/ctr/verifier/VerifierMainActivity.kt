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
    private val appStatusViewModel: AppConfigViewModel by viewModel()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val dialogUtil: DialogUtil by inject()
    private val intentUtil: IntentUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setProductionFlags()

        observeStatuses()
    }

    private fun observeStatuses() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        introductionViewModel.introductionStatusLiveData.observe(this, EventObserver {
            navController.navigate(R.id.action_introduction, IntroductionFragment.getBundle(it))
        })

        appStatusViewModel.appStatusLiveData.observe(this, EventObserver {
            handleAppStatus(it, navController)
        })
    }

    private fun handleAppStatus(
        appStatus: AppStatus,
        navController: NavController
    ) {
        if ( appStatus is AppStatus.UpdateRecommended) {
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

    override fun onStart() {
        super.onStart()
        // Only get app config on every app foreground when introduction is finished
        if (introductionViewModel.getIntroductionStatus() is IntroductionStatus.IntroductionFinished) {
            appStatusViewModel.refresh(mobileCoreWrapper)
        }
    }
}
