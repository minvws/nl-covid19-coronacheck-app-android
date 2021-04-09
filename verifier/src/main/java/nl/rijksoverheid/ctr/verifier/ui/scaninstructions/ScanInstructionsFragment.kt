package nl.rijksoverheid.ctr.verifier.ui.scaninstructions

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.ctr.appconfig.AppConfigUtil
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerUtil
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainFragment
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanInstructionsBinding
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanInstructionsFragment : Fragment(R.layout.fragment_scan_instructions) {

    private val appConfigUtil: AppConfigUtil by inject()
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

        val binding = FragmentScanInstructionsBinding.bind(view)
        binding.button.setOnClickListener {
            openScanner()
        }

        GroupAdapter<GroupieViewHolder>()
            .run {
                addAll(
                    listOf(
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_1_title,
                            description = getString(R.string.scan_instructions_1_description),
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_2_title,
                            description = getString(R.string.scan_instructions_2_description),
                            image = R.drawable.illustration_scan_instruction_2
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_3_title,
                            description = getString(R.string.scan_instructions_3_description),
                            image = R.drawable.illustration_scan_instruction_3
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_4_title,
                            description = appConfigUtil.getStringWithTestValidity(R.string.scan_instructions_4_description)
                        )
                    )
                )
                binding.recyclerView.adapter = this
            }

        scanQrViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as VerifierMainFragment).presentLoading(it)
        })

        scanQrViewModel.validatedQrLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(ScanInstructionsFragmentDirections.actionScanResult(it))
        })
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
