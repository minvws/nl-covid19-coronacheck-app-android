package nl.rijksoverheid.ctr.verifier.ui.policy

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.*
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentVerificationPolicySelectionBinding
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

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

    private val args: VerificationPolicySelectionFragmentArgs by navArgs()
    private val scannerUtil: ScannerUtil by inject()
    private val viewModel: VerificationPolicySelectionViewModel by viewModel()

    private val dialogUtil: DialogUtil by inject()
    private val verifierCachedAppConfigUseCase: VerifierCachedAppConfigUseCase by inject()
    private val verificationPolicySelectionStateUseCase: VerificationPolicySelectionStateUseCase by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentVerificationPolicySelectionBinding.bind(view)

        binding.policy3G.text = getString(R.string.verifier_risksetting_title, VerificationPolicy3G.configValue)
        binding.policy1G.text = getString(R.string.verifier_risksetting_title, VerificationPolicy1G.configValue)

        binding.link.setOnClickListener {
            getString(R.string.verifier_risksetting_start_readmore_url).launchUrl(requireContext())
        }

        viewModel.scannerUsedRecentlyLiveData.observe(viewLifecycleOwner, EventObserver {
            setupScreenBasedOnType(it)
        })

        viewModel.didScanRecently()
    }

    private fun setupScreenBasedOnType(scanUsedRecently: Boolean) {
        val selectionType = args.selectionType
        when (selectionType) {
            is VerificationPolicySelectionType.FirstTimeUse -> setupScreenForFirstTimeUse()
            is VerificationPolicySelectionType.Default -> setupScreenForDefaultSelectionType(selectionType.state.verificationPolicySelectionState, scanUsedRecently)
        }
        setupRadioGroup(selectionType)
    }

    private fun setupScreenForDefaultSelectionType(verificationPolicySelectionState: VerificationPolicySelectionState, scanUsedRecently: Boolean) {
        binding.subHeader.setHtmlText(
            htmlText =
            if (verificationPolicySelectionState != VerificationPolicySelectionState.Selection.None && scanUsedRecently) {
                getString(
                    R.string.verifier_risksetting_menu_scan_settings_selected_title,
                    TimeUnit.SECONDS.toMinutes(verifierCachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong())
                )
            } else {
                getString(R.string.verifier_risksetting_menu_scan_settings_unselected_title)
            }
        )
        binding.link.visibility = GONE
        binding.confirmationButton.setOnClickListener {
            onConfirmationButtonClicked {
                presentWarningDialog()
            }
        }
        binding.confirmationButton.text = getString(R.string.verifier_risksetting_confirmation_button)
    }

    private fun presentWarningDialog() {
        val currentPolicyState = verificationPolicySelectionStateUseCase.get()
        val policyHasNotChanged = currentPolicyState is VerificationPolicySelectionState.Selection.Policy1G && binding.policy1G.isChecked ||
                currentPolicyState is VerificationPolicySelectionState.Selection.Policy3G && binding.policy3G.isChecked
        when {
            currentPolicyState is VerificationPolicySelectionState.Selection.None -> {
                storeSelection()
                closeScreen()
            }
            policyHasNotChanged -> closeScreen()
            else -> dialogUtil.presentDialog(
                context = requireContext(),
                title = R.string.verifier_risksetting_confirmation_dialog_title,
                message = getString(
                    R.string.verifier_risksetting_confirmation_dialog_message,
                    TimeUnit.SECONDS.toMinutes(verifierCachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong())
                ),
                positiveButtonText = R.string.verifier_risksetting_confirmation_dialog_positive_button,
                positiveButtonCallback = {
                    storeSelection()
                    closeScreen()
                },
                negativeButtonText = R.string.verifier_risksetting_confirmation_dialog_negative_button
            )
        }
    }

    private fun closeScreen() {
        findNavControllerSafety()?.popBackStack(R.id.nav_policy_settings, true)
    }

    private fun setupScreenForFirstTimeUse() {
        binding.subHeader.setHtmlText(R.string.verifier_risksetting_firsttimeuse_header)
        binding.confirmationButton.text = getString(R.string.scan_qr_button)
        binding.confirmationButton.setOnClickListener {
            onConfirmationButtonClicked {
                storeSelection()
                findNavControllerSafety()?.popBackStack(R.id.nav_scan_qr, false)
                scannerUtil.launchScanner(requireActivity(), arguments?.getString("returnUri"))
            }
        }
        binding.header.visibility = VISIBLE
    }

    private fun onConfirmationButtonClicked(onClick: () -> Unit) {
        val policySelected = binding.policy3G.isChecked || binding.policy1G.isChecked
        if (policySelected) {
            onClick()
        } else {
            toggleError(true)
        }
    }

    private fun storeSelection() {
        val policy = when {
            binding.policy3G.isChecked -> VerificationPolicy3G
            binding.policy1G.isChecked -> VerificationPolicy1G
            else -> return
        }
        viewModel.storeSelection(policy)
    }

    private fun allRadioButtons() = listOf(binding.policy3G, binding.policy1G)

    private fun toggleError(error: Boolean) {
        if (error) {
            binding.errorContainer.visibility = VISIBLE
            allRadioButtons().forEach {
                it.buttonTintList =
                    ColorStateList.valueOf(requireContext().getColor(R.color.error))
            }

            // scroll all the way down so the user notices the error
            binding.scroll.post {
                if (isAdded) {
                    binding.scroll.fullScroll(FOCUS_DOWN)
                }
            }
        } else {
            binding.errorContainer.visibility = GONE
            allRadioButtons().forEach {
                it.buttonTintList =
                    ColorStateList.valueOf(requireContext().getColor(R.color.link))
            }
        }
    }

    private fun setupRadioGroup(selectionType: VerificationPolicySelectionType) {
        policySelected(selectionType.state.verificationPolicySelectionState)

        when (viewModel.radioButtonSelected) {
            binding.policy3G.id -> policy3GSelected()
            binding.policy1G.id -> policy1GSelected()
        }

        binding.policy3GContainer.setOnClickListener {
            policy3GSelected()
        }

        binding.policy1GContainer.setOnClickListener {
            policy1GSelected()
        }
    }

    private fun policy3GSelected() {
        policyChecked(binding.policy3G.id)
        policySelected(VerificationPolicySelectionState.Selection.Policy3G)
    }

    private fun policy1GSelected() {
        policyChecked(binding.policy1G.id)
        policySelected(VerificationPolicySelectionState.Selection.Policy1G)
    }

    private fun policySelected(state: VerificationPolicySelectionState) {
        when (state) {
            VerificationPolicySelectionState.Policy3G,
            VerificationPolicySelectionState.Selection.Policy3G -> {
                binding.policy3G.isChecked = true
                binding.policy1G.isChecked = false
            }
            VerificationPolicySelectionState.Policy1G,
            VerificationPolicySelectionState.Selection.Policy1G -> {
                binding.policy3G.isChecked = false
                binding.policy1G.isChecked = true
            }
            VerificationPolicySelectionState.Selection.None -> {
                binding.policy3G.isChecked = false
                binding.policy1G.isChecked = false
            }
        }
    }

    private fun policyChecked(checkedId: Int) {
        toggleError(false)

        viewModel.updateRadioButton(checkedId)
    }
}
