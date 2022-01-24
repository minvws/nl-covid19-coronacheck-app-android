package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentDccScanResultBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultInvalidData


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DccScanResultFragment: Fragment(R.layout.fragment_dcc_scan_result) {

    private val navArgs: DccScanResultFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDccScanResultBinding.bind(view)

        binding.header.text = getString(navArgs.data.header)
        binding.description.text = getString(navArgs.data.description)
        binding.bottom.setButtonText(getString(navArgs.data.buttonText))
        binding.bottom.setSecondaryButtonText(getString(navArgs.data.secondaryButtonText))

        binding.bottom.setButtonClick {
            navigateSafety(DccScanResultFragmentDirections.actionNavQrScanner(
                previousScanResult = navArgs.data.previousScanResult,
            ))
        }

        binding.bottom.setSecondaryButtonClick {
            navigateSafety(DccScanResultFragmentDirections.actionScanResultInvalid(ScanResultInvalidData.Error("")))
        }
    }
}