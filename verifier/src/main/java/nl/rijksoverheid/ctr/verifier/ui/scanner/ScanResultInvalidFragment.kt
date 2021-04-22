package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.design.ext.enableCustomLinks
import nl.rijksoverheid.ctr.design.ext.enableWebLinks
import nl.rijksoverheid.ctr.design.utils.getSpannableFromHtml
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.util.MultiTapDetector
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultInvalidBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultInvalidData
import nl.rijksoverheid.ctr.verifier.ui.scanner.util.ScannerUtil
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanResultInvalidFragment : Fragment(R.layout.fragment_scan_result_invalid) {

    private val args: ScanResultInvalidFragmentArgs by navArgs()
    private val scannerUtil: ScannerUtil by inject()

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable {
        findNavController().navigate(ScanResultInvalidFragmentDirections.actionNavMain())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanResultInvalidBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(ScanResultInvalidFragmentDirections.actionNavMain())
        }

        binding.subtitle.text = getString(R.string.scan_result_invalid_subtitle).fromHtml()

        binding.subtitle.enableCustomLinks {
            findNavController().navigate(ScanResultInvalidFragmentDirections.actionShowInvalidExplanation())
        }

        binding.button.setOnClickListener {
            scannerUtil.launchScanner(requireActivity())
        }

        val invalidData = args.invalidData
        MultiTapDetector(binding.image) { amount, _ ->
            if (amount == 3) {
                when (invalidData) {
                    is ScanResultInvalidData.Invalid -> {
                        presentDebugDialog(invalidData.verifiedQr.getDebugHtmlString())
                    }
                    is ScanResultInvalidData.Error -> {
                        presentDebugDialog(invalidData.error)
                    }
                }
            }
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

    override fun onResume() {
        super.onResume()
        val autoCloseDuration =
            if (BuildConfig.FLAVOR == "tst") TimeUnit.SECONDS.toMillis(10) else TimeUnit.MINUTES.toMillis(
                3
            )
        autoCloseHandler.postDelayed(autoCloseRunnable, autoCloseDuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoCloseHandler.removeCallbacks(autoCloseRunnable)
    }
}
