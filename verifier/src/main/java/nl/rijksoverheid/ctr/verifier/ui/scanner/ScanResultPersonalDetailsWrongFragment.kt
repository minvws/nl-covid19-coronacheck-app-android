/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultPersonalDetailsWrongBinding

class ScanResultPersonalDetailsWrongFragment :
    Fragment(R.layout.fragment_scan_result_personal_details_wrong) {

    private val args: ScanResultPersonalDetailsWrongFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentScanResultPersonalDetailsWrongBinding.bind(view).run {
            toolbar.setNavigationOnClickListener { findNavControllerSafety()?.popBackStack() }
            bottom.setButtonClick { setButtonNavigation() }
            if (args.externalReturnAppData != null) bottom.setIcon(R.drawable.ic_deeplink)
        }
    }

    private fun setButtonNavigation() {
        args.externalReturnAppData?.let {
            try {
                startActivity(it.intent)
                activity?.finishAffinity()
            } catch (exception: ActivityNotFoundException) {
                navigateToScanner()
            }
        } ?: navigateToScanner()
    }

    private fun navigateToScanner() {
        navigateSafety(
            ScanResultPersonalDetailsWrongFragmentDirections.actionNavQrScanner()
        )
    }
}