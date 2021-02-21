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
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AppStatusFragment : Fragment(R.layout.fragment_app_status) {

    private val viewModel: AppStatusViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAppStatusBinding.bind(view)

        viewModel.appStatus.observe(viewLifecycleOwner) {
            when (it) {
                is AppStatus.Deactivated -> {
                    binding.bind(
                        R.string.app_status_deactivated_title,
                        it.message,
                        R.string.app_status_deactivated_action
                    ) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.informationUrl)))
                    }
                }
                is AppStatus.UpdateRequired -> {
                    binding.bind(
                        R.string.app_status_update_required_title,
                        it.message ?: getString(R.string.app_status_update_required_message),
                        R.string.app_status_update_required_action
                    ) {
                        openPlayStore()
                    }
                }
                else -> {
                    /* nothing */
                }
            }
        }
    }

    private fun openPlayStore() {
        val pkg = requireContext().packageName
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            .setPackage("com.android.vending")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // fall back to browser intent
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                )
            )
        }
    }
}

private fun FragmentAppStatusBinding.bind(
    @StringRes title: Int,
    message: String,
    @StringRes action: Int,
    onClick: () -> Unit
) {
    this.title.setText(title)
    this.message.text = message
    this.action.setText(action)
    this.action.setOnClickListener {
        onClick()
    }
}
