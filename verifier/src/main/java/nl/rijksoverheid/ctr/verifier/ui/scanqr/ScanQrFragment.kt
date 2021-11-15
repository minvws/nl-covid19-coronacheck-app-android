package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainActivity
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanQrBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
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

    private val scanQrViewModel: ScanQrViewModel by viewModel()
    private val scannerUtil: ScannerUtil by inject()
    private val clockDeviationUseCase: ClockDeviationUseCase by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanQrBinding.bind(view)
        binding.instructionsButton.setOnClickListener {
            navigateSafety(R.id.nav_scan_qr, ScanQrFragmentDirections.actionScanInstructions())
        }

        binding.bottom.setButtonClick {
            goToNextScreen()
        }

        binding.clockdeviationView.clockdeviationButton.setOnClickListener {
            infoFragmentUtil.presentAsBottomSheet(childFragmentManager, InfoFragmentData.TitleDescription(
                title = getString(R.string.clockdeviation_page_title),
                descriptionData = DescriptionData(R.string.clockdeviation_page_message, customLinkIntent = Intent(Settings.ACTION_DATE_SETTINGS)),
            ))
        }
        // Handle clock deviation view
        observeServerTimeSynced(binding)
        showDeviationViewIfNeeded(binding)

        checkPendingDeeplink()
    }

    private fun checkPendingDeeplink() {
        (activity as? VerifierMainActivity)?.returnUri?.let {
            goToNextScreen()
        }
    }

    private fun goToNextScreen() {
        when (scanQrViewModel.getNextScannerScreenState()) {
            NextScannerScreenState.Instructions -> findNavController().navigate(ScanQrFragmentDirections.actionScanInstructions())
            NextScannerScreenState.RiskModeSelection -> scannerUtil.launchModeSelection(requireActivity())
            NextScannerScreenState.Scanner -> scannerUtil.launchScanner(requireActivity())
        }
    }

    /**
     * Whenever the server time is synced we want to check
     * if we want to inform the user that the clock is not correct
     */
    private fun observeServerTimeSynced(binding: FragmentScanQrBinding) {
        clockDeviationUseCase.serverTimeSyncedLiveData.observe(viewLifecycleOwner, EventObserver {
            showDeviationViewIfNeeded(binding)
        })
    }

    private fun showDeviationViewIfNeeded(binding: FragmentScanQrBinding){
        binding.clockdeviationView.root.isGone = !clockDeviationUseCase.hasDeviation()
    }
}


