package nl.rijksoverheid.ctr.verifier.ui.policy

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy2G
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy3G
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentVerificationPolicySelectionBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerificationPolicySelectionFragment :
    Fragment(R.layout.fragment_verification_policy_selection) {

    private var _binding: FragmentVerificationPolicySelectionBinding? = null
    private val binding get() = _binding!!

    private val scannerUtil: ScannerUtil by inject()
    private val viewModel: VerificationPolicySelectionViewModel by viewModel {
        parametersOf(arguments?.getBoolean(isScanQRFlow) == true)
    }

    private val dialogUtil: DialogUtil by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationPolicySelectionBinding.inflate(inflater)

        binding.link.setOnClickListener {
            getString(R.string.verifier_risksetting_start_readmore_url).launchUrl(requireContext())
        }

        viewModel.policyFlowLiveData.observe(viewLifecycleOwner, ::onVerificationFlowUpdate)

        return binding.root
    }

    private fun onVerificationFlowUpdate(flow: VerificationPolicyFlow) {
        when (flow) {
            is VerificationPolicyFlow.ScanQR -> setupScreenForScanQrFlow()
            is VerificationPolicyFlow.Settings -> setupScreenForSettingsFlow(flow.state)
        }
        setupRadioGroup(flow)
    }

    private fun setupScreenForSettingsFlow(verificationPolicyState: VerificationPolicyState) {
        binding.subHeader.setHtmlText(
            if (verificationPolicyState != VerificationPolicyState.None) {
                R.string.verifier_risksetting_menu_scan_settings_selected_title
            } else {
                R.string.verifier_risksetting_menu_scan_settings_unselected_title
            }
        )
        binding.link.visibility = GONE
        binding.confirmationButton.setOnClickListener {
            dialogUtil.presentDialog(
                context = requireContext(),
                title = R.string.verifier_risksetting_confirmation_dialog_title,
                message = getString(R.string.verifier_risksetting_confirmation_dialog_message),
                positiveButtonText = R.string.verifier_risksetting_confirmation_dialog_positive_button,
                positiveButtonCallback = {
                    onConfirmationButtonClicked {
                        navigateSafety(R.id.nav_scan_qr)
                    }
                },
                negativeButtonText = R.string.verifier_risksetting_confirmation_dialog_negative_button
            )
        }
    }

    private fun setupScreenForScanQrFlow() {
        binding.toolbar.visibility = VISIBLE

        binding.toolbar.setNavigationOnClickListener {
            findNavControllerSafety()?.popBackStack()
        }
        binding.confirmationButton.setOnClickListener {
            onConfirmationButtonClicked {
                findNavControllerSafety()?.popBackStack()
                scannerUtil.launchScanner(requireActivity())
            }
        }
        binding.header.visibility = VISIBLE
    }

    private fun onConfirmationButtonClicked(navigationAction: () -> Unit) {
        val policySelected = binding.verificationPolicyRadioGroup.checkedRadioButtonId != NO_ID
        if (policySelected) {
            storeSelection()
            navigationAction()
        } else {
            toggleError(true)
        }
    }

    private fun storeSelection() {
        viewModel.storeSelection(
            if (binding.verificationPolicyRadioGroup.checkedRadioButtonId == binding.policy2G.id) {
                VerificationPolicy2G
            } else {
                VerificationPolicy3G
            }
        )
    }

    private fun toggleError(error: Boolean) {
        if (error) {
            binding.errorContainer.visibility = VISIBLE
            binding.policy2G.buttonTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.error))
            binding.policy3G.buttonTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.error))

            // scroll all the way down so the user notices the error
            binding.scroll.post {
                if (isAdded) {
                    binding.scroll.fullScroll(FOCUS_DOWN)
                }
            }
        } else {
            binding.errorContainer.visibility = GONE
            binding.policy2G.buttonTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.link))
            binding.policy3G.buttonTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.link))
        }
    }

    private fun setupRadioGroup(flow: VerificationPolicyFlow) {
        binding.verificationPolicyRadioGroup.check(
            when (flow.state) {
                VerificationPolicyState.Policy2G -> binding.policy2G.id
                VerificationPolicyState.Policy3G -> binding.policy3G.id
                VerificationPolicyState.None -> NO_ID
            }
        )

        viewModel.radioButtonLiveData.observe(viewLifecycleOwner) {
            binding.verificationPolicyRadioGroup.check(it)
        }

        binding.verificationPolicyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            toggleError(false)

            if (flow is VerificationPolicyFlow.Settings) {
                binding.subHeader.setHtmlText(R.string.verifier_risksetting_menu_scan_settings_selected_title)
            }
            viewModel.updateRadioButton(checkedId)
        }

        binding.policy3GContainer.setOnClickListener {
            binding.verificationPolicyRadioGroup.check(R.id.policy3G)
        }

        binding.policy2GContainer.setOnClickListener {
            binding.verificationPolicyRadioGroup.check(R.id.policy2G)
        }
    }

    companion object {
        const val isScanQRFlow = "IS_SCAN_QR_FLOW"
    }
}
