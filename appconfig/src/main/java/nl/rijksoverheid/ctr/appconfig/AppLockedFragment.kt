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
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.appconfig.databinding.FragmentAppLockedBinding
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject

class AppLockedFragment : Fragment(R.layout.fragment_app_locked) {

    private val args: AppLockedFragmentArgs by navArgs()

    private val androidUtil: AndroidUtil by inject()
    private val intentUtil: IntentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAppLockedBinding.bind(view)

        if (androidUtil.isSmallScreen()) {
            binding.illustration.visibility = View.GONE
        }

        when (args.appStatus) {
            is AppStatus.Deactivated -> {
                binding.bind(
                    R.string.app_status_deactivated_title,
                    R.string.app_status_deactivated_message,
                    R.string.app_status_deactivated_action,
                    R.drawable.illustration_app_status_deactivated
                ) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(
                                getString(
                                    if (isVerifierApp(requireContext())) {
                                        R.string.verifier_deactivation_url
                                    } else {
                                        R.string.holder_deactivation_url
                                    }
                                )
                            )
                        )
                    )
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

private fun FragmentAppLockedBinding.bind(
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
