package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.design.FullScreenDialogFragment
import nl.rijksoverheid.ctr.design.utils.getSpannableFromHtml
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.util.MultiTapDetector
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultBinding
import nl.rijksoverheid.ctr.verifier.models.VerifiedQr
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState
import org.koin.android.ext.android.inject
import java.time.ZonedDateTime


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanResultBinding.bind(view)

        val validatedQrResultState = args.validatedResult
        if (validatedQrResultState is VerifiedQrResultState.Valid) {
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            binding.image.setImageResource(R.drawable.illustration_scan_result_valid)
            binding.title.text = getString(R.string.scan_result_valid_title)
            binding.subtitle.text = getString(R.string.scan_result_valid_subtitle).fromHtml()

            validatedQrResultState.verifiedQr.testResultAttributes.let {
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
                            validatedQrResultState.verifiedQr
                        )
                    )
                }
            }
        } else {
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.image.setImageResource(R.drawable.illustration_scan_result_invalid)
            binding.title.text = getString(R.string.scan_result_invalid_title)
            binding.subtitle.text = getString(R.string.scan_result_invalid_subtitle).fromHtml()

            binding.subtitle.setOnClickListener {
                findNavController().navigate(ScanResultFragmentDirections.actionShowInvalidExplanation())
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.button.setOnClickListener {
            setFragmentResult(
                ScanQrFragment.REQUEST_KEY,
                bundleOf(ScanQrFragment.EXTRA_LAUNCH_SCANNER to true)
            )
            findNavController().popBackStack()
        }

        if (BuildConfig.FLAVOR != "prod") {
            MultiTapDetector(binding.image) { amount, _ ->
                if (amount == 3) {
                    when (validatedQrResultState) {
                        is VerifiedQrResultState.Valid -> presentDebugDialog(validatedQrResultState.verifiedQr)
                        is VerifiedQrResultState.Invalid -> presentDebugDialog(
                            validatedQrResultState.verifiedQr
                        )
                    }
                }
            }
        }
    }

    private fun presentDebugDialog(verifiedQr: VerifiedQr?) {
        val message = verifiedQr?.toString() ?: "ClCore error"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(ZonedDateTime.now().toString())
            .setMessage(
                getSpannableFromHtml(requireContext(), message)
            )
            .show()
    }
}
