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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultPersonalDetailsWrongBinding

class ScanResultPersonalDetailsWrongFragment :
    Fragment(R.layout.fragment_scan_result_personal_details_wrong) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentScanResultPersonalDetailsWrongBinding.bind(view).run {
            toolbar.setNavigationOnClickListener { findNavControllerSafety()?.popBackStack() }
            button.setOnClickListener {
                navigateSafety(
                    ScanResultPersonalDetailsWrongFragmentDirections.actionNavQrScanner()
                )
            }
        }
    }
}