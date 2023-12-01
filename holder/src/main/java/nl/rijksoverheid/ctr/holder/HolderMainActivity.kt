/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import nl.rijksoverheid.ctr.design.utils.DialogButtonData
import nl.rijksoverheid.ctr.design.utils.DialogFragmentData
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.SharedDialogFragment
import nl.rijksoverheid.ctr.holder.databinding.ActivityMainBinding
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModel
import nl.rijksoverheid.ctr.holder.ui.priority_notification.PriorityNotificationViewModel
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
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

    private val deviceRootedViewModel: DeviceRootedViewModel by viewModel()
    private val deviceSecureViewModel: DeviceSecureViewModel by viewModel()
    private val priorityNotificationViewModel: PriorityNotificationViewModel by viewModel()
    private val dialogUtil: DialogUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_DayNight)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.FLAVOR.lowercase().contains("prod")) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

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

        priorityNotificationViewModel.showPriorityNotificationLiveData.observe(this, EventObserver {
            SharedDialogFragment.show(
                supportFragmentManager, DialogFragmentData(
                    text = it,
                    positiveButtonData = DialogButtonData.Dismiss(R.string.ok)
                )
            )
        })
    }
}
