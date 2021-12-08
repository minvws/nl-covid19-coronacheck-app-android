package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentVerificationPolicyInfoBinding
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

    private val verificationPolicyStateUseCase: VerificationPolicyStateUseCase by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVerificationPolicyInfoBinding.inflate(inflater)

        setupButtons()

        setupPolicy()

        return binding.root
    }

    private fun setupButtons() {
        binding.adjustButton.setOnClickListener {
            navigateSafety(VerificationPolicyInfoFragmentDirections.actionPolicySelection())
        }

        binding.readMoreButton.setOnClickListener {
            getString(R.string.verifier_risksetting_start_readmore_url).launchUrl(requireContext())
        }

        binding.bottom.setButtonClick {
            navigateSafety(VerificationPolicyInfoFragmentDirections.actionPolicySelection())
        }
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
        when (verificationPolicyStateUseCase.get()) {
            VerificationPolicyState.None -> {}
            VerificationPolicyState.Policy2G -> {
                displayPolicyViews(R.string.verifier_start_scan_qr_policy_indication_2g, R.string.verifier_risksetting_highrisk_subtitle)
            }
            VerificationPolicyState.Policy3G -> {
                displayPolicyViews(R.string.verifier_start_scan_qr_policy_indication_3g, R.string.verifier_risksetting_lowrisk_subtitle)
            }
        }
    }
}