package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentPaperScanResultBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultInvalidData


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PaperScanResultFragment: Fragment(R.layout.fragment_paper_scan_result) {

    private val navArgs: PaperScanResultFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPaperScanResultBinding.bind(view)

        binding.header.text = getString(navArgs.data.header)
        binding.description.text = getString(navArgs.data.description)
        binding.bottom.setButtonText(getString(navArgs.data.buttonText))
        binding.bottom.customiseSecondaryButton { it.text = getString(navArgs.data.secondaryButtonText) }

        binding.bottom.setButtonClick {
            navigateSafety(PaperScanResultFragmentDirections.actionNavQrScanner())
        }

        binding.bottom.setSecondaryButtonClick {
            navigateSafety(PaperScanResultFragmentDirections.actionScanResultInvalid(ScanResultInvalidData.Error("")))
        }
    }
}