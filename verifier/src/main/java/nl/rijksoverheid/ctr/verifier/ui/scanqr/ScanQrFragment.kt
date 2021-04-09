package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerUtil
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainFragment
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanQrBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrFragment : Fragment(R.layout.fragment_scan_qr) {

    private val qrCodeScannerUtil: QrCodeScannerUtil by inject()
    private val scanQrViewModel: ScanQrViewModel by viewModel()

    private val qrScanResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val scanResult = qrCodeScannerUtil.parseScanResult(it.data)
            if (scanResult != null) {
                scanQrViewModel.validate(
                    qrContent = scanResult
                )
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanQrBinding.bind(view)
        binding.description.text = binding.description.text.toString().fromHtml()
        binding.description.setOnClickListener {
            findNavController().navigate(ScanQrFragmentDirections.actionScanInstructions())
        }

        scanQrViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            (requireActivity() as VerifierMainFragment).presentLoading(it)
        })

        scanQrViewModel.validatedQrLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(ScanQrFragmentDirections.actionScanResult(it))
        })

        binding.button.setOnClickListener {
            if (!scanQrViewModel.scanInstructionsSeen()) {
                findNavController().navigate(ScanQrFragmentDirections.actionScanInstructions())
            } else {
                openScanner()
            }
        }
    }

    private fun openScanner() {
        qrCodeScannerUtil.launchScanner(
            requireActivity(), qrScanResult,
            getString(
                R.string.scanner_custom_title
            ), getString(
                R.string.scanner_custom_message
            ),
            getString(R.string.camera_rationale_dialog_title),
            getString(R.string.camera_rationale_dialog_description),
            getString(R.string.ok)
        )
    }
}


