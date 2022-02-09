package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentNewPolicyRulesBinding
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NewPolicyRulesFragment : Fragment(R.layout.fragment_new_policy_rules) {

    private val newPolicyRulesViewModel: NewPolicyRulesViewModel by viewModel()
    private val scannerUtil: ScannerUtil by inject()
    private val androidUtil: AndroidUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNewPolicyRulesBinding.bind(view)

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        } else {
            binding.image.visibility = View.VISIBLE
        }

        setObservers(binding)

        newPolicyRulesViewModel.init()
    }

    private fun setObservers(binding: FragmentNewPolicyRulesBinding) {
        newPolicyRulesViewModel.scannerNavigationStateEvent.observe(
            viewLifecycleOwner, EventObserver { nextScreen(it) })
        newPolicyRulesViewModel.newPolicyRules.observe(viewLifecycleOwner) {
            setBindings(binding, it)
        }
    }

    private fun nextScreen(state: ScannerNavigationState) {
        when (state) {
            is ScannerNavigationState.Scanner -> {
                if (!state.isLocked) {
                    findNavControllerSafety()?.popBackStack(R.id.nav_scan_qr, false)
                    scannerUtil.launchScanner(requireActivity(), arguments?.getString("returnUri"))
                } else {
                    navigateToHome()
                }
            }
            is ScannerNavigationState.VerificationPolicySelection -> {
                navigateSafety(
                    NewPolicyRulesFragmentDirections.actionPolicySelection(
                        selectionType = VerificationPolicySelectionType.FirstTimeUse(
                            ScannerState.Unlocked(
                                VerificationPolicySelectionState.Selection.None
                            )
                        ),
                        toolbarTitle = getString(R.string.verifier_menu_risksetting),
                        returnUri = arguments?.getString("returnUri"),
                    )
                )
            }
            else -> navigateToHome()
        }
    }

    private fun navigateToHome() {
        navigateSafety(
            R.id.nav_scan_qr,
            bundleOf("returnUri" to arguments?.getString("returnUri"))
        )
    }

    private fun setBindings(binding: FragmentNewPolicyRulesBinding, item: NewPolicyItem) {
        binding.title.text = getString(item.title)
        binding.description.setHtmlText(getString(item.description), false)
        binding.bottomButtonBar.setButtonClick { newPolicyRulesViewModel.nextScreen() }
    }
}
