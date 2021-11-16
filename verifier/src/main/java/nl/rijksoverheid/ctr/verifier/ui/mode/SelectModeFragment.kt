package nl.rijksoverheid.ctr.verifier.ui.mode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentModeSelectBinding
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
class SelectModeFragment: Fragment(R.layout.fragment_mode_select) {

    private val scannerUtil: ScannerUtil by inject()
    private val persistenceManager: PersistenceManager by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentModeSelectBinding.inflate(inflater)

        if (arguments?.getBoolean(addToolbarArgument) == true) {
            setupToolbar(binding)
        } else {
            binding.subHeader.text = getString(R.string.risk_mode_selection_subtitle_from_menu)
        }

        setupRadioGroup(binding)

        return binding.root
    }

    private fun setupToolbar(binding: FragmentModeSelectBinding) {
        binding.toolbar.visibility = VISIBLE

        binding.toolbar.setNavigationOnClickListener {
            findNavControllerSafety()?.popBackStack()
        }
        binding.startScanningButton.visibility = VISIBLE
        binding.startScanningButton.setOnClickListener {
            findNavControllerSafety()?.popBackStack()
            scannerUtil.launchScanner(requireActivity())
        }
        binding.header.visibility = VISIBLE
    }

    private fun setupRadioGroup(binding: FragmentModeSelectBinding) {
        if (persistenceManager.isRiskModeSelectionSet()) {
            binding.modeRadioGroup.check(if (persistenceManager.getHighRiskModeSelected()) {
                binding.highRisk.id
            } else {
                binding.lowRisk.id
            })
        } else {
            persistenceManager.setHighRiskModeSelected(false)
        }

        binding.modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val highRiskModeSelected = checkedId == binding.highRisk.id
            persistenceManager.setHighRiskModeSelected(highRiskModeSelected)
        }

        binding.subtitle3g.setOnClickListener {
            binding.modeRadioGroup.check(R.id.lowRisk)
        }
        binding.subtitle2g.setOnClickListener {
            binding.modeRadioGroup.check(R.id.highRisk)
        }
    }

    companion object {
        const val addToolbarArgument = "ADD_TOOLBAR_ARGUMENT"
    }
}