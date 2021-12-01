package nl.rijksoverheid.ctr.verifier.ui.mode

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.*
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentVerificationPolicySelectionBinding
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject

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
    private val persistenceManager: PersistenceManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationPolicySelectionBinding.inflate(inflater)

        if (arguments?.getBoolean(addToolbarArgument) == true) {
            setupToolbar()
        } else {
            binding.subHeader.text = getString(R.string.risk_mode_selection_subtitle_from_menu)
        }

        binding.link.setOnClickListener {
            // TODO navigate to rijksoverheid link when decided
        }

        setupRadioGroup(savedInstanceState?.getBoolean(errorStateKey))

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding.let {
            outState.putBoolean(errorStateKey, binding.errorContainer.visibility == VISIBLE)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.visibility = VISIBLE

        binding.toolbar.setNavigationOnClickListener {
            findNavControllerSafety()?.popBackStack()
        }
        binding.confirmationButton.setOnClickListener {
            val modeSelected = binding.verificationPolicyRadioGroup.checkedRadioButtonId != NO_ID
            if (modeSelected) {
                findNavControllerSafety()?.popBackStack()
                storeSelection()
                scannerUtil.launchScanner(requireActivity())
            } else {
                toggleError(true)
            }
        }
        binding.header.visibility = VISIBLE
    }

    private fun storeSelection() {
        persistenceManager.setVerificationPolicySelected(
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
        } else {
            binding.errorContainer.visibility = GONE
            binding.policy2G.buttonTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.link))
            binding.policy3G.buttonTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.link))
        }
    }

    private fun setupRadioGroup(errorOnBeforeScreenRotation: Boolean?) {
        persistenceManager.getVerificationPolicySelected()?.let {
            binding.verificationPolicyRadioGroup.check(
                if (it == VerificationPolicy2G) {
                    binding.policy2G.id
                } else {
                    binding.policy3G.id
                }
            )
        }

        binding.verificationPolicyRadioGroup.setOnCheckedChangeListener { _, _ ->
            toggleError(false)
        }

        binding.subtitle3g.setOnClickListener {
            binding.verificationPolicyRadioGroup.check(R.id.policy3G)
        }

        binding.subtitle2g.setOnClickListener {
            binding.verificationPolicyRadioGroup.check(R.id.policy2G)
        }

        if (errorOnBeforeScreenRotation == true) {
            toggleError(true)
        }
    }

    companion object {
        const val addToolbarArgument = "ADD_TOOLBAR_ARGUMENT"
        private const val errorStateKey = "ERROR_STATE_KEY"
    }
}
