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
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.MainNavDirections
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentVerificationPolicySettingsBinding
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerificationPolicySettingsFragment : Fragment(R.layout.fragment_verification_policy_settings) {

    private var _binding: FragmentVerificationPolicySettingsBinding? = null
    private val binding get() = _binding!!

    private val persistenceManager: PersistenceManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVerificationPolicySettingsBinding.inflate(inflater)

        setupButtons()

        setupPolicy()

        return binding.root
    }

    private fun setupButtons() {
        binding.adjustButton.setOnClickListener {
            navigateSafety(VerificationPolicySettingsFragmentDirections.actionPolicySelection())
        }

        binding.readMoreButton.setOnClickListener {
            getString(R.string.verifier_risksetting_start_readmore_url).launchUrl(requireContext())
        }

        binding.bottom.setButtonClick {
            navigateSafety(VerificationPolicySettingsFragmentDirections.actionPolicySelection())
        }
    }

    private fun setupPolicy() {
        persistenceManager.getVerificationPolicySelected()?.let {
            binding.bottom.visibility = GONE
            binding.adjustButton.visibility = VISIBLE
            binding.separator1.visibility = VISIBLE
            binding.separator2.visibility = VISIBLE
            binding.policySettingHeader.visibility = VISIBLE
            binding.policySettingBody.visibility = VISIBLE
            when (it) {
                VerificationPolicy.VerificationPolicy2G -> {
                    binding.policySettingHeader.text = getString(R.string.verifier_start_scan_qr_policy_indication_2g)
                    binding.policySettingBody.text = getString(R.string.verifier_risksetting_highrisk_subtitle)
                }
                VerificationPolicy.VerificationPolicy3G -> {
                    binding.policySettingHeader.text = getString(R.string.verifier_start_scan_qr_policy_indication_3g)
                    binding.policySettingBody.text = getString(R.string.verifier_risksetting_lowrisk_subtitle)
                }
            }
        }
    }
}