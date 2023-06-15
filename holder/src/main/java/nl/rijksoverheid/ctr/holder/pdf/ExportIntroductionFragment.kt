/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.pdf

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentExportIntroductionBinding
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

class ExportIntroductionFragment: Fragment(R.layout.fragment_export_introduction) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentExportIntroductionBinding.bind(view)
        binding.bottom.setButtonClick {
            navigateSafety(ExportIntroductionFragmentDirections.actionPdfWebview())
        }
    }
}
