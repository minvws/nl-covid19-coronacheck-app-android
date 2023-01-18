/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design.menu.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.BuildConfig
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.AboutThisAppRowBinding
import nl.rijksoverheid.ctr.design.databinding.AboutThisAppSectionBinding
import nl.rijksoverheid.ctr.design.databinding.FragmentAboutAppBinding
import nl.rijksoverheid.ctr.shared.DebugDisclosurePolicyPersistenceManager
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getParcelableCompat
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.Environment
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton
import org.koin.android.ext.android.inject

class AboutThisAppFragment : Fragment(R.layout.fragment_about_app) {

    private val policyPersistenceManager: DebugDisclosurePolicyPersistenceManager by inject()

    companion object {
        private const val EXTRA_ABOUT_THIS_APP_DATA = "data"
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAboutAppBinding.bind(view)

        val aboutThisAppData =
            arguments?.getParcelableCompat<AboutThisAppData>(EXTRA_ABOUT_THIS_APP_DATA)
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
                        is AboutThisAppData.Destination -> findNavControllerSafety()?.navigate(
                            item.destinationId,
                            item.arguments
                        )
                    }
                }
            }
        }

        // we have this button in the layout twice because of the design requirement
        // to align it to the bottom when the content is not scrollable
        // or follow the scrolling content otherwise
        binding.aboutThisAppBottomButton.customiseSecondaryButton {
            it.setStrokeColorResource(R.color.error)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            it.setOnClickListener {
                showClearAppDataDialog()
            }
        }
        binding.aboutThisAppBottomButton.customiseButton {
            it.visibility = GONE
        }

        binding.resetButtonContainerWhenScrollable.customiseSecondaryButton {
            it.setStrokeColorResource(R.color.error)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            it.setOnClickListener {
                showClearAppDataDialog()
            }
        }
        binding.resetButtonContainerWhenScrollable.customiseButton {
            it.visibility = GONE
        }

        // On acceptance builds show button to trigger deeplink to scanner
        if (!aboutThisAppData.deeplinkScannerUrl.isNullOrEmpty()) {
            bindScannerDeeplinkButton(
                deeplinkScannerButton = binding.deeplinkScannerButton,
                url = aboutThisAppData.deeplinkScannerUrl
            )
        }

        // On test and acceptance builds show buttons to set policy locally
        if (BuildConfig.DEBUG || Environment.get(requireContext()) is Environment.Acc || Environment.get(
                requireContext()
            ) is Environment.Tst
        ) {
            bindDebugPolicyButtons(binding)
        }

        positionResetButton(binding)
    }

    private fun positionResetButton(binding: FragmentAboutAppBinding) {
        binding.aboutThisAppScrollview.doOnPreDraw {
            val canScroll = binding.aboutThisAppScrollview.canScrollVertically(1)
            binding.aboutThisAppBottomButton.isVisible = !canScroll
            binding.resetButtonContainerWhenScrollable.isVisible = canScroll
        }
    }

    private fun showClearAppDataDialog() {
        arguments?.getParcelableCompat<AboutThisAppData>(EXTRA_ABOUT_THIS_APP_DATA)?.resetAppDialogDirection?.let {
            findNavControllerSafety()?.navigate(it.destinationId, it.arguments)
        }
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
            zeroGPolicyButton.setOnClickListener {
                policyPersistenceManager.setDebugDisclosurePolicy(DisclosurePolicy.ZeroG)
                restartApp()
            }
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
        val intent =
            context?.packageManager?.getLaunchIntentForPackage(requireContext().packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}
