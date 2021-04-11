package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.design.FullScreenDialogFragment
import nl.rijksoverheid.ctr.design.utils.getSpannableFromHtml
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.util.MultiTapDetector
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultBinding
import nl.rijksoverheid.ctr.verifier.models.VerifiedQr
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.ui.scanner.util.ScannerUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanResultFragment : FullScreenDialogFragment(R.layout.fragment_scan_result) {

    private val args: ScanResultFragmentArgs by navArgs()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()
    private val scannerUtil: ScannerUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanResultBinding.bind(view)

        handleValidatedResult(
            binding = binding,
            validatedQrResultState = args.validatedResult
        )

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(ScanResultFragmentDirections.actionNavMain())
        }

        binding.button.setOnClickListener {
            scannerUtil.launchScanner(requireActivity())
        }
    }

    private fun handleValidatedResult(
        binding: FragmentScanResultBinding,
        validatedQrResultState: VerifiedQrResultState
    ) {
        when (validatedQrResultState) {
            is VerifiedQrResultState.Valid -> {
                presentValidScreen(
                    binding = binding,
                    verifiedQr = validatedQrResultState.verifiedQr,
                    backgroundColor = R.color.green,
                    title = R.string.scan_result_valid_title
                )
            }
            is VerifiedQrResultState.Demo -> {
                presentValidScreen(
                    binding = binding,
                    verifiedQr = validatedQrResultState.verifiedQr,
                    backgroundColor = R.color.grey_medium,
                    title = R.string.scan_result_demo_title
                )
            }
            is VerifiedQrResultState.Invalid -> {
                presentInvalidScreen(
                    binding = binding
                )
            }
            is VerifiedQrResultState.Error -> {
                presentInvalidScreen(
                    binding = binding
                )
            }
        }

        MultiTapDetector(binding.image) { amount, _ ->
            if (amount == 3) {
                when (validatedQrResultState) {
                    is VerifiedQrResultState.Valid -> presentDebugDialog(validatedQrResultState.verifiedQr.getDebugHtmlString())
                    is VerifiedQrResultState.Invalid -> presentDebugDialog(
                        validatedQrResultState.verifiedQr.getDebugHtmlString()
                    )
                    is VerifiedQrResultState.Error -> presentDebugDialog(
                        validatedQrResultState.error
                    )
                    is VerifiedQrResultState.Demo -> {
                        presentDebugDialog(validatedQrResultState.verifiedQr.getDebugHtmlString())
                    }
                }
            }
        }
    }

    private fun presentValidScreen(
        binding: FragmentScanResultBinding,
        verifiedQr: VerifiedQr,
        @ColorRes backgroundColor: Int,
        @StringRes title: Int
    ) {
        binding.root.setBackgroundResource(backgroundColor)
        binding.image.setImageResource(R.drawable.illustration_scan_result_valid)
        binding.title.setText(title)
        binding.subtitle.text =
            getString(R.string.scan_result_valid_subtitle).fromHtml()

        verifiedQr.testResultAttributes.let {
            val personalDetails = personalDetailsUtil.getPersonalDetails(
                it.firstNameInitial,
                it.lastNameInitial,
                it.birthDay,
                it.birthMonth
            )
            binding.personalDetailsHolder.setPersonalDetails(personalDetails)
            binding.personalDetailsHolder.visibility = View.VISIBLE

            binding.subtitle.setOnClickListener {
                findNavController().navigate(
                    ScanResultFragmentDirections.actionShowValidExplanation(
                        verifiedQr
                    )
                )
            }
        }
    }

    private fun presentInvalidScreen(binding: FragmentScanResultBinding) {
        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.red
            )
        )
        binding.image.setImageResource(R.drawable.illustration_scan_result_invalid)
        binding.title.text = getString(R.string.scan_result_invalid_title)
        binding.subtitle.text =
            getString(R.string.scan_result_invalid_subtitle).fromHtml()

        binding.subtitle.setOnClickListener {
            findNavController().navigate(ScanResultFragmentDirections.actionShowInvalidExplanation())
        }
    }

    private fun presentDebugDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Debug Info")
            .setMessage(getSpannableFromHtml(requireContext(), message))
            .setPositiveButton(
                "Ok"
            ) { _, _ -> }
            .show()
    }
}
