package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentVerificationPolicyInfoBinding
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerificationPolicyInfoFragment : Fragment(R.layout.fragment_verification_policy_info) {

    private var _binding: FragmentVerificationPolicyInfoBinding? = null
    private val binding get() = _binding!!

    private val scannerStateUseCase: ScannerStateUseCase by inject()
    private val verificationPolicySelectionStateUseCase: VerificationPolicySelectionStateUseCase by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentVerificationPolicyInfoBinding.bind(view)

        setupButtons()

        setupPolicy()
    }

    private fun setupButtons() {
        binding.adjustButton.setOnClickListener {
            openPolicySelection()
        }

        binding.readMoreButton.setOnClickListener {
            getString(R.string.verifier_risksetting_start_readmore_url).launchUrl(requireContext())
        }

        binding.bottom.setButtonClick {
            openPolicySelection()
        }
    }

    private fun openPolicySelection() {
        val policySelectionAction = VerificationPolicyInfoFragmentDirections.actionPolicySelection(
            selectionType = VerificationPolicySelectionType.Default(scannerStateUseCase.get()),
            toolbarTitle = getString(
                if (verificationPolicySelectionStateUseCase.get() is VerificationPolicySelectionState.Selection.None) {
                    R.string.verifier_menu_risksetting
                } else {
                    R.string.verifier_risksetting_changeselection_title
                }
            ),
        )
        navigateSafety(policySelectionAction)
    }

    private fun displayPolicyViews(verificationPolicy: VerificationPolicy, bodyTextStringId: Int) {
        binding.bottom.visibility = GONE
        binding.adjustButton.visibility = VISIBLE
        binding.separator1.visibility = VISIBLE
        binding.separator2.visibility = VISIBLE
        binding.policySettingHeader.visibility = VISIBLE
        binding.policySettingBody.visibility = VISIBLE
        binding.policySettingHeader.text =
            getString(R.string.verifier_risksetting_changeselection, verificationPolicy.configValue)
        binding.policySettingBody.text = getString(bodyTextStringId)
    }

    private fun setupPolicy() {
        when (scannerStateUseCase.get().verificationPolicySelectionState) {
            VerificationPolicySelectionState.Selection.None -> {
            }
            VerificationPolicySelectionState.Policy1G,
            VerificationPolicySelectionState.Selection.Policy1G -> {
                displayPolicyViews(
                    VerificationPolicy.VerificationPolicy1G,
                    R.string.verifier_risksetting_subtitle_1G
                )
            }
            VerificationPolicySelectionState.Policy3G,
            VerificationPolicySelectionState.Selection.Policy3G -> {
                displayPolicyViews(
                    VerificationPolicy.VerificationPolicy3G,
                    R.string.verifier_risksetting_subtitle_3G
                )
            }
        }
    }
}