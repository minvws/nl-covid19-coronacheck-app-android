package nl.rijksoverheid.ctr.verifier.scanqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.util.MLKitQrCodeScannerUtil
import nl.rijksoverheid.ctr.verifier.BaseFragment
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanQrBinding
import nl.rijksoverheid.ctr.verifier.scaninstructions.ScanInstructionsDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrFragment : BaseFragment() {

    private lateinit var binding: FragmentScanQrBinding
    private val qrCodeScannerUtil: MLKitQrCodeScannerUtil by inject()
    private val scanQrViewModel: ScanQrViewModel by viewModel()
    private val args: ScanQrFragmentArgs by navArgs()

    private val qrScanResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val scanResult = qrCodeScannerUtil.parseScanResult(it.data)
            if (scanResult != null) {
                scanQrViewModel.validate(
                    qrContent = scanResult
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanQrBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.description.text = binding.description.text.toString().fromHtml()
        binding.description.setOnClickListener {
            findNavController().navigate(ScanQrFragmentDirections.actionScanInstructions())
        }

        scanQrViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            presentLoading(it)
        })

        scanQrViewModel.qrValidLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(ScanQrFragmentDirections.actionScanResult(it))
        })

        setFragmentResultListener(
            ScanInstructionsDialogFragment.REQUEST_KEY
        ) { requestKey, bundle ->
            if (requestKey == ScanInstructionsDialogFragment.REQUEST_KEY && bundle.getBoolean(
                    ScanInstructionsDialogFragment.EXTRA_LAUNCH_SCANNER
                )
            ) {
                openScanner()
            }
        }

        if (args.openScanner) {
            openScanner()
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
            requireActivity() as AppCompatActivity, qrScanResult, getString(
                R.string.scanner_custom_message
            )
        )
    }
}


