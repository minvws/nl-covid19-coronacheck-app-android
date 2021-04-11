package nl.rijksoverheid.ctr.verifier.scanner

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierQrScannerFragment : QrCodeScannerFragment() {

    private val scanQrViewModel: ScanQrViewModel by viewModel()

    override fun onQrScanned(content: String) {
        scanQrViewModel.validate(
            qrContent = content
        )
    }

    override fun getCopy(): Copy {
        return Copy(
            title = getString(R.string.scanner_custom_title),
            message = getString(R.string.scanner_custom_message),
            rationaleDialog = Copy.RationaleDialog(
                title = getString(R.string.camera_rationale_dialog_title),
                description = getString(R.string.camera_rationale_dialog_description),
                okayButtonText = getString(R.string.ok)
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanQrViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        })

        scanQrViewModel.validatedQrLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(VerifierQrScannerFragmentDirections.actionScanResult(it))
        })
    }
}
