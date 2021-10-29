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
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.BuildConfig
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.AboutThisAppRowBinding
import nl.rijksoverheid.ctr.design.databinding.FragmentAboutAppBinding
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTimeNumerical
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton
import org.koin.android.ext.android.inject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset


class AboutThisAppFragment : Fragment(R.layout.fragment_about_app) {

    private val dialogUtil: DialogUtil by inject()

    companion object {
        private const val EXTRA_ABOUT_THIS_APP_DATA = "EXTRA_ABOUT_THIS_APP_DATA"

        fun getBundle(data: AboutThisAppData): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_ABOUT_THIS_APP_DATA, data)
            return bundle
        }
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAboutAppBinding.bind(view)

        val aboutThisAppData = arguments?.getParcelable<AboutThisAppData>(EXTRA_ABOUT_THIS_APP_DATA)
            ?: throw IllegalStateException("AboutThisAppData should be set")

        aboutThisAppData.readMoreItems.forEach { item ->
            val view = AboutThisAppRowBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.readMoreItems,
                true
            )

            view.title.text = item.text

            view.root.setAsAccessibilityButton(true)
            view.root.contentDescription = item.text

            view.root.setOnClickListener {
                when (item) {
                    is AboutThisAppData.Url -> item.url.launchUrl(requireContext())
                    is AboutThisAppData.ClearAppData -> showClearAppDataDialog()
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

        // On test and acceptance builds show button to trigger deeplink to scanner
        if (BuildConfig.DEBUG || context?.packageName == "nl.rijksoverheid.ctr.holder.acc") {
            bindScannerDeeplinkButton(binding.deeplinkScannerButton)
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

    private fun bindScannerDeeplinkButton(deeplinkScannerButton: Button) {
        deeplinkScannerButton.visibility = View.VISIBLE
        val link =
            "https://web.acc.coronacheck.nl/verifier/scan?returnUri=https://web.acc.coronacheck.nl/app/open?returnUri=scanner-test"
        deeplinkScannerButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(link) })
        }
    }
}
