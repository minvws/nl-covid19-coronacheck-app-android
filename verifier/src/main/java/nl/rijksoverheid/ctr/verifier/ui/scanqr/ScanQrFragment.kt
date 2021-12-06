package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy2G
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy3G
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainActivity
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanQrBinding
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySwitchState
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

    private var _binding: FragmentScanQrBinding? = null
    private val binding get() = _binding!!

    private val scanQrViewModel: ScanQrViewModel by viewModel()
    private val scannerUtil: ScannerUtil by inject()
    private val clockDeviationUseCase: ClockDeviationUseCase by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentScanQrBinding.bind(view)
        binding.instructionsButton.setOnClickListener {
            navigateSafety(R.id.nav_scan_qr, ScanQrFragmentDirections.actionScanInstructions())
        }

        binding.bottom.setButtonClick {
            scanQrViewModel.nextScreen()
        }

        scanQrViewModel.nextScreenEvent.observe(viewLifecycleOwner, ::goToNextScreen)

        scanQrViewModel.liveData.observe(viewLifecycleOwner, ::onStateUpdated)

        scanQrViewModel.timerLiveData.observe(viewLifecycleOwner, ::updateTitle)

        binding.clockdeviationView.clockdeviationButton.setOnClickListener {
            infoFragmentUtil.presentAsBottomSheet(
                childFragmentManager, InfoFragmentData.TitleDescription(
                    title = getString(R.string.clockdeviation_page_title),
                    descriptionData = DescriptionData(
                        R.string.clockdeviation_page_message,
                        customLinkIntent = Intent(Settings.ACTION_DATE_SETTINGS)
                    ),
                )
            )
        }
        // Handle clock deviation view
        observeServerTimeSynced()
        showDeviationViewIfNeeded()

        scanQrViewModel.onViewCreated()

        checkPendingDeeplink()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onStateUpdated(state: ScanQRState) {
        binding.image.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), when (state.policy) {
                    VerificationPolicyState.None -> {
                        binding.bottom.hidePolicyIndication()
                        R.drawable.illustration_scanner_get_started_3g
                    }
                    VerificationPolicyState.Policy2G -> {
                        binding.bottom.setPolicy(VerificationPolicy2G)
                        R.drawable.illustration_scanner_get_started_2g
                    }
                    VerificationPolicyState.Policy3G -> {
                        binding.bottom.setPolicy(VerificationPolicy3G)
                        R.drawable.illustration_scanner_get_started_3g
                    }
                }
            )
        )

        when (state.lock) {
            is VerificationPolicySwitchState.Locked -> lockScanner()
            is VerificationPolicySwitchState.Unlocked -> unlockScanner()
        }
    }

    // if we opened the scanner app via a deep link from another app
    // and we haven't went through either the onboarding or
    // the scan instructions flow, we need to remember the
    // deep link return uri to return back to the other app
    // after we're done with scanning
    private fun checkPendingDeeplink() {
        (activity as? VerifierMainActivity)?.returnUri?.let {
            scanQrViewModel.nextScreen()
        }
    }

    private fun goToNextScreen(nextScreenEvent: Event<NextScannerScreenState>) {
        when (nextScreenEvent.peekContent()) {
            NextScannerScreenState.Instructions -> findNavController().navigate(
                ScanQrFragmentDirections.actionScanInstructions()
            )
            NextScannerScreenState.VerificationPolicySelection -> scannerUtil.launchVerificationPolicySelection(
                requireActivity()
            )
            NextScannerScreenState.Scanner -> scannerUtil.launchScanner(requireActivity())
        }
    }

    /**
     * Whenever the server time is synced we want to check
     * if we want to inform the user that the clock is not correct
     */
    private fun observeServerTimeSynced() {
        clockDeviationUseCase.serverTimeSyncedLiveData.observe(viewLifecycleOwner, EventObserver {
            showDeviationViewIfNeeded()
        })
    }

    private fun showDeviationViewIfNeeded() {
        binding.clockdeviationView.root.isGone = !clockDeviationUseCase.hasDeviation()
    }

    private fun lockScanner() {
        binding.instructionsButton.visibility = GONE
        binding.clockdeviationView.root.visibility = GONE
        binding.description.setText(R.string.verifier_home_countdown_subtitle)
        binding.bottom.lock()
    }

    private fun unlockScanner() {
        binding.title.setText(R.string.scan_qr_header)
        binding.description.setText(R.string.scan_qr_description)
        binding.instructionsButton.visibility = VISIBLE
        showDeviationViewIfNeeded()
        binding.bottom.unlock()
    }

    private fun updateTitle(timeLeft: String) {
        binding.title.text = getString(R.string.verifier_home_countdown_title, timeLeft)
    }
}
