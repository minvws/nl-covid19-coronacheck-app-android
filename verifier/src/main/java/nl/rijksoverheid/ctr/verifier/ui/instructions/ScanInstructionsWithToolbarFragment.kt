/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.instructions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanInstructionsWithToolbarBinding

// Wrapper class adding a toolbar to the instructions fragment
class ScanInstructionsWithToolbarFragment :
    Fragment(R.layout.fragment_scan_instructions_with_toolbar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanInstructionsWithToolbarBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener {
          findNavController().popBackStack()
        }
    }
}