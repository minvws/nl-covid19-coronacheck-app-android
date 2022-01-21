package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
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
    private val verificationPolicyStateUseCase: VerificationPolicyStateUseCase by inject()

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
                if (verificationPolicyStateUseCase.get() != VerificationPolicyState.None) {
                    R.string.verifier_risksetting_changeselection_title
                } else {
                    R.string.verifier_menu_risksetting
                }
            ),
        )
        navigateSafety(policySelectionAction)
    }

    private fun displayPolicyViews(headerTextStringId: Int, bodyTextStringId: Int) {
        binding.bottom.visibility = GONE
        binding.adjustButton.visibility = VISIBLE
        binding.separator1.visibility = VISIBLE
        binding.separator2.visibility = VISIBLE
        binding.policySettingHeader.visibility = VISIBLE
        binding.policySettingBody.visibility = VISIBLE
        binding.policySettingHeader.text = getString(headerTextStringId)
        binding.policySettingBody.text = getString(bodyTextStringId)
    }

    private fun setupPolicy() {
        when (scannerStateUseCase.get().verificationPolicyState) {
            VerificationPolicyState.None -> {
            }
            VerificationPolicyState.Policy1G -> {
                // TODO change copies used
                displayPolicyViews(
                    R.string.verifier_start_scan_qr_policy_indication_2g,
                    R.string.verifier_risksetting_highrisk_subtitle
                )
            }
            VerificationPolicyState.Policy3G -> {
                displayPolicyViews(
                    R.string.verifier_start_scan_qr_policy_indication_3g,
                    R.string.verifier_risksetting_lowrisk_subtitle
                )
            }
        }
    }
}