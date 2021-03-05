/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.appconfig.databinding.FragmentAppStatusBinding
import nl.rijksoverheid.ctr.appconfig.model.AppStatus

class AppStatusFragment : Fragment(R.layout.fragment_app_status) {

    companion object {
        const val EXTRA_APP_STATUS = "EXTRA_APP_STATUS"
    }

    private val appStatusStrings by lazy { (requireActivity().application as AppStatusStringProvider).getAppStatusStrings() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAppStatusBinding.bind(view)
        val appStatus = arguments?.getParcelable<AppStatus>(EXTRA_APP_STATUS)
            ?: throw IllegalStateException("AppStatus should not be null")

        when (appStatus) {
            is AppStatus.Deactivated -> {
                binding.bind(
                    appStatusStrings.appStatusDeactivatedTitle,
                    appStatusStrings.appStatusDeactivatedMessage,
                    appStatusStrings.appStatusDeactivatedAction
                ) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appStatus.informationUrl)))
                }
            }
            is AppStatus.UpdateRequired -> {
                binding.bind(
                    appStatusStrings.appStatusUpdateRequiredTitle,
                    appStatusStrings.appStatusUpdateRequiredMessage,
                    appStatusStrings.appStatusUpdateRequiredAction
                ) {
                    openPlayStore()
                }
            }
            is AppStatus.InternetRequired -> {
                binding.bind(
                    appStatusStrings.appStatusInternetRequiredTitle,
                    appStatusStrings.appStatusInternetRequiredMessage,
                    appStatusStrings.appStatusInternetRequiredAction
                ) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.coronacheck.nl")
                        )
                    )
                }
            }
            else -> {
                /* nothing */
            }
        }

    }

    private fun openPlayStore() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${requireContext().packageName}")
        )
            .setPackage("com.android.vending")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // fall back to browser intent
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                )
            )
        }
    }
}

private fun FragmentAppStatusBinding.bind(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes action: Int,
    onClick: () -> Unit
) {
    this.title.setText(title)
    this.message.setText(message)
    this.action.setText(action)
    this.action.setOnClickListener {
        onClick()
    }
}
