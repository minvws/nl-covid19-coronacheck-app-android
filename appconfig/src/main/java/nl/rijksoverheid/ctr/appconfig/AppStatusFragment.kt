/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.appconfig.databinding.FragmentAppStatusBinding
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.shared.utils.IntentUtil
import org.koin.android.ext.android.inject

class AppStatusFragment : Fragment(R.layout.fragment_app_status) {

    companion object {
        const val EXTRA_APP_STATUS = "EXTRA_APP_STATUS"

        fun getBundle(appStatus: AppStatus): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_APP_STATUS, appStatus)
            return bundle
        }
    }

    private val androidUtil: AndroidUtil by inject()
    private val intentUtil: IntentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAppStatusBinding.bind(view)
        val appStatus = arguments?.getParcelable<AppStatus>(EXTRA_APP_STATUS)
            ?: throw IllegalStateException("AppStatus should not be null")

        if (androidUtil.isSmallScreen()) {
            binding.illustration.visibility = View.GONE
        }

        when (appStatus) {
            is AppStatus.Deactivated -> {
                binding.bind(
                    R.string.app_status_deactivated_title,
                    R.string.app_status_deactivated_message,
                    R.string.app_status_deactivated_action,
                    R.drawable.illustration_app_status_deactivated
                ) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appStatus.informationUrl)))
                }
            }
            is AppStatus.UpdateRequired -> {
                binding.bind(
                    R.string.app_status_update_required_title,
                    R.string.app_status_update_required_message,
                    R.string.app_status_update_required_action,
                    R.drawable.illustration_app_status_update_required
                ) {
                    intentUtil.openPlayStore(requireContext())
                }
            }
            is AppStatus.Error -> {
                binding.bind(
                    R.string.app_status_internet_required_title,
                    R.string.app_status_internet_required_message,
                    R.string.app_status_internet_required_action,
                    R.drawable.illustration_app_status_internet_required
                ) {
                    val launchIntent =
                        requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)
                    launchIntent?.let {
                        launchIntent.flags =
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        requireContext().startActivity(launchIntent)
                    }
                }
            }
            else -> {
                /* nothing */
            }
        }

    }
}

private fun FragmentAppStatusBinding.bind(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes action: Int,
    @DrawableRes illustration: Int,
    onClick: () -> Unit
) {
    this.title.setText(title)
    this.message.setText(message)
    this.action.setText(action)
    this.illustration.setImageResource(illustration)
    this.action.setOnClickListener {
        onClick()
    }
}
