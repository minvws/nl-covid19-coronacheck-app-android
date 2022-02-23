/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design.menu.about

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.BuildConfig
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.AboutThisAppRowBinding
import nl.rijksoverheid.ctr.design.databinding.AboutThisAppSectionBinding
import nl.rijksoverheid.ctr.design.databinding.FragmentAboutAppBinding
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTimeNumerical
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.shared.DebugDisclosurePolicyPersistenceManager
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton
import org.koin.android.ext.android.inject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class AboutThisAppFragment : Fragment(R.layout.fragment_about_app) {

    private val dialogUtil: DialogUtil by inject()
    private val policyPersistenceManager: DebugDisclosurePolicyPersistenceManager by inject()

    companion object {
        private const val EXTRA_ABOUT_THIS_APP_DATA = "data"
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAboutAppBinding.bind(view)

        val aboutThisAppData = arguments?.getParcelable<AboutThisAppData>(EXTRA_ABOUT_THIS_APP_DATA)
            ?: throw IllegalStateException("AboutThisAppData should be set")

        aboutThisAppData.sections.forEach {
            val sectionView = AboutThisAppSectionBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.sections,
                true
            )

            sectionView.header.text = getString(it.header)
            it.items.forEach { item ->
                val itemView = AboutThisAppRowBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    sectionView.items,
                    true
                )

                itemView.title.text = item.text

                itemView.root.setAsAccessibilityButton(true)
                itemView.root.contentDescription = item.text

                itemView.root.setOnClickListener {
                    when (item) {
                        is AboutThisAppData.Url -> item.url.launchUrl(requireContext())
                        is AboutThisAppData.ClearAppData -> showClearAppDataDialog()
                        is AboutThisAppData.Destination -> findNavControllerSafety()?.navigate(item.destinationId)
                    }
                }
            }
        }

        binding.appVersion.text = getString(
            R.string.app_version,
            aboutThisAppData.versionName,
            aboutThisAppData.versionCode
        )

        binding.configVersion.text = getString(
            R.string.config_version,
            aboutThisAppData.configVersionHash,
            OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(aboutThisAppData.configVersionTimestamp),
                ZoneOffset.UTC
            ).formatDayMonthYearTimeNumerical()
        )

        // On acceptance builds show button to trigger deeplink to scanner
        if (!aboutThisAppData.deeplinkScannerUrl.isNullOrEmpty()) {
            bindScannerDeeplinkButton(
                deeplinkScannerButton = binding.deeplinkScannerButton,
                url = aboutThisAppData.deeplinkScannerUrl
            )
        }

        // On test and acceptance builds show buttons to set policy locally
        if (BuildConfig.DEBUG || context?.packageName == "nl.rijksoverheid.ctr.holder.acc") {
            bindDebugPolicyButtons(binding)
        }
    }

    private fun showClearAppDataDialog() {
        dialogUtil.presentDialog(
            context = requireContext(),
            title = R.string.about_this_app_clear_data_title,
            message = resources.getString(R.string.about_this_app_clear_data_description),
            negativeButtonText = R.string.about_this_app_clear_data_cancel,
            positiveButtonText = R.string.about_this_app_clear_data_confirm,
            positiveButtonCallback = ::clearAppData
        )
    }

    private fun clearAppData() {
        (context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
    }

    private fun bindScannerDeeplinkButton(deeplinkScannerButton: Button, url: String) {
        deeplinkScannerButton.visibility = View.VISIBLE
        deeplinkScannerButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
        }
    }

    private fun bindDebugPolicyButtons(binding: FragmentAboutAppBinding) {
        with(binding) {
            policyButtons.isVisible = true
            oneGPolicyButton.setOnClickListener {
                policyPersistenceManager.setDebugDisclosurePolicy(DisclosurePolicy.OneG)
                restartApp()
            }
            threeGPolicyButton.setOnClickListener {
                policyPersistenceManager.setDebugDisclosurePolicy(DisclosurePolicy.ThreeG)
                restartApp()
            }
            oneGThreeGPolicyButton.setOnClickListener {
                policyPersistenceManager.setDebugDisclosurePolicy(DisclosurePolicy.OneAndThreeG)
                restartApp()
            }
            configPolicyButton.setOnClickListener {
                policyPersistenceManager.setDebugDisclosurePolicy(null)
                restartApp()
            }
        }
    }

    private fun restartApp() {
        val intent = context?.packageManager?.getLaunchIntentForPackage(requireContext().packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}
