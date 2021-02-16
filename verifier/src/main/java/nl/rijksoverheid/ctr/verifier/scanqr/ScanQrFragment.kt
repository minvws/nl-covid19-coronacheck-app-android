package nl.rijksoverheid.ctr.verifier.scanqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.zxing.integration.android.IntentIntegrator
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.ext.observeResult
import nl.rijksoverheid.ctr.shared.util.QrCodeScannerUtil
import nl.rijksoverheid.ctr.verifier.BaseFragment
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanQrBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrFragment : BaseFragment() {

    private lateinit var binding: FragmentScanQrBinding
    private val qrCodeScannerUtil: QrCodeScannerUtil by inject()
    private val scanQrViewModel: ScanQrViewModel by viewModel()
    private val args: ScanQrFragmentArgs by navArgs()

    private val qrScanResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val result = IntentIntegrator.parseActivityResult(
                IntentIntegrator.REQUEST_CODE,
                it.resultCode,
                it.data
            )
            if (result.contents != null) {
                scanQrViewModel.validate(
                    currentDate = OffsetDateTime.now(),
                    qrContent = result.contents
                )
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanQrBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.description.text = binding.description.text.toString().fromHtml()

        observeResult(scanQrViewModel.qrValidLiveData, {
            presentLoading(true)
        }, {
            presentLoading(false)
            findNavController().navigate(ScanQrFragmentDirections.actionScanResult(it))
        }, {
            presentLoading(false)
            presentError()
        })

        if (args.openScanner) {
            openScanner()
        }

        binding.button.setOnClickListener {
            openScanner()
        }
    }

    private fun openScanner() {
        qrCodeScannerUtil.launchScanner(requireActivity() as AppCompatActivity, qrScanResult)
    }
}
