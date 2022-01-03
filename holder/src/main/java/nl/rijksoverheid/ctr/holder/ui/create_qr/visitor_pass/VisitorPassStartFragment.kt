package nl.rijksoverheid.ctr.holder.ui.create_qr.visitor_pass

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentVisitorPassStartBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.VerificationCodeFragmentData
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VisitorPassStartFragment : Fragment(R.layout.fragment_visitor_pass_start) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentVisitorPassStartBinding.bind(view)
        binding.button.setOnClickListener {
            navigateSafety(
                VisitorPassStartFragmentDirections.actionVisitorPassCode(
                    toolbarTitle = getString(R.string.visitorpass_start_action),
                    data = VerificationCodeFragmentData.VisitorPass,
                )
            )
        }
    }
}
