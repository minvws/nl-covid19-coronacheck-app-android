/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.databinding.ActivityMainBinding
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModel
import nl.rijksoverheid.ctr.introduction.IntroductionFragment
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.shared.utils.IntentUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderMainActivity : AppCompatActivity() {

    private val introductionViewModel: IntroductionViewModel by viewModel()
    private val appConfigViewModel: AppConfigViewModel by viewModel()
    private val deviceRootedViewModel: DeviceRootedViewModel by viewModel()
    private val deviceSecureViewModel: DeviceSecureViewModel by viewModel()
    private val dialogUtil: DialogUtil by inject()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val intentUtil: IntentUtil by inject()
    private var isFreshStart: Boolean = true // track if this is a fresh start of the app
    private val androidUtil: AndroidUtil by inject()
    private val connectivityChangeCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                appConfigViewModel.refresh(mobileCoreWrapper)
            }
        }
    private val networkChangeFilter = NetworkRequest.Builder().build() // blank filter for all networks

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.FLAVOR.lowercase().contains("prod")) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        introductionViewModel.introductionStatusLiveData.observe(this, EventObserver {
            navController.navigate(
                R.id.action_introduction, IntroductionFragment.getBundle(it)
            )
        })

        appConfigViewModel.appStatusLiveData.observe(this, {
            handleAppStatus(it, navController)
        })

        deviceRootedViewModel.deviceRootedLiveData.observe(this, EventObserver {
            if (it) {
                dialogUtil.presentDialog(
                    context = this,
                    title = R.string.dialog_rooted_device_title,
                    message = getString(R.string.dialog_rooted_device_message),
                    positiveButtonText = R.string.dialog_rooted_device_positive_button,
                    positiveButtonCallback = { },
                    onDismissCallback = { deviceRootedViewModel.setHasDismissedRootedDeviceDialog() }
                )
            }
        })

        deviceSecureViewModel.deviceSecureLiveData.observe(this, EventObserver {
            if (!it) {
                dialogUtil.presentDialog(
                    context = this,
                    title = R.string.dialog_device_secure_warning_title,
                    message = getString(R.string.dialog_device_secure_warning_description),
                    positiveButtonText = R.string.dialog_device_secure_positive_button,
                    positiveButtonCallback = { },
                    onDismissCallback = {
                        deviceSecureViewModel.setHasDismissedUnsecureDeviceDialog(
                            true
                        )
                    }
                )
            }
        })


    }

    private fun handleAppStatus(
        appStatus: AppStatus,
        navController: NavController
    ) {
        if (appStatus is AppStatus.UpdateRecommended) {
            showRecommendedUpdateDialog()
            return
        }

        if (appStatus !is AppStatus.NoActionRequired) {
            navController.navigate(R.id.action_app_status, AppStatusFragment.getBundle(appStatus))
        }
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

    override fun onStart() {
        super.onStart()
        // Only get app config on every app foreground when introduction is finished
        if (introductionViewModel.getIntroductionStatus() is IntroductionStatus.IntroductionFinished) {
            appConfigViewModel.refresh(mobileCoreWrapper, isFreshStart)
            isFreshStart = false
        }
    }

    override fun onResume() {
        super.onResume()
        // Add connectivity change listener. If a network is detected try to refresh the config
        androidUtil.getConnectivityManager().registerNetworkCallback(
            networkChangeFilter, connectivityChangeCallback
        )

    }

    override fun onPause() {
        super.onPause()
        androidUtil.getConnectivityManager().unregisterNetworkCallback(connectivityChangeCallback)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Handle if an external app sets the launch mode of this activity to single top of single task.
        // In this case we need to set the graph again and handle the deeplink ourselves so that the entire
        // graph is traversed to find the deeplink
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.holder_nav_graph_root)
        navController.handleDeepLink(intent)
    }

    fun launchUrl(url: String) {
        url.launchUrl(this) {
            dialogUtil.presentDialog(
                context = this,
                title = R.string.dialog_no_browser_title,
                // remove the https prefix to make it more eye friendsly
                message = getString(R.string.dialog_no_browser_message, url).replace(
                    "https://",
                    ""
                ),
                positiveButtonText = R.string.ok,
                positiveButtonCallback = {},
            )
        }
    }
}

fun Context.launchUrl(url: String) {
    val holderMainActivity = this as? HolderMainActivity
    holderMainActivity?.launchUrl(url)
}
