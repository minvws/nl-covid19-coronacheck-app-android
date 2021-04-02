package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.BaseActivity
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerUtil
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainActivity
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

    private val introductionViewModel: IntroductionViewModel by viewModel()
    private val qrCodeScannerUtil: QrCodeScannerUtil by inject()
    private val scanQrViewModel: ScanQrViewModel by viewModel()

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val EXTRA_LAUNCH_SCANNER = "LAUNCH_SCANNER"
    }

    private val qrScanResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val scanResult = qrCodeScannerUtil.parseScanResult(it.data)
            if (scanResult != null) {
                scanQrViewModel.validate(
                    qrContent = scanResult
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!introductionViewModel.introductionFinished()) {
            findNavController().navigate(R.id.action_introduction)
        } else if (requireActivity() is BaseActivity) {
            (requireActivity() as BaseActivity).removeSplashScreen()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentScanQrBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentScanQrBinding.bind(view)
        binding.description.text = binding.description.text.toString().fromHtml()

        scanQrViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            (requireActivity() as VerifierMainActivity).presentLoading(it)
        })

        scanQrViewModel.validatedQrLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(ScanQrFragmentDirections.actionScanResult(it))
        })

        setFragmentResultListener(
            REQUEST_KEY
        ) { requestKey, bundle ->
            if (requestKey == REQUEST_KEY && bundle.getBoolean(
                    EXTRA_LAUNCH_SCANNER
                )
            ) {
                openScanner()
            }
        }

        binding.button.setOnClickListener {
            if (!scanQrViewModel.scanInstructionsSeen()) {
                findNavController().navigate(ScanQrFragmentDirections.actionScanInstructions(true))
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


