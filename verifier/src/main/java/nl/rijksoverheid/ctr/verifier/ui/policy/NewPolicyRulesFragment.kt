package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentNewPolicyRulesBinding
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NewPolicyRulesFragment : Fragment(R.layout.fragment_new_policy_rules) {

    private val androidUtil: AndroidUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNewPolicyRulesBinding.bind(view)

        binding.title.text = getString(R.string.new_policy_1G_title)
        binding.description.setHtmlText(getString(R.string.new_policy_1G_subtitle), false)

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        } else {
            binding.image.visibility = View.VISIBLE
            binding.image.setImageResource(R.drawable.illustration_scanner_get_started_1g)
        }

        binding.bottomButtonBar.setButtonClick {
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
    }
}
