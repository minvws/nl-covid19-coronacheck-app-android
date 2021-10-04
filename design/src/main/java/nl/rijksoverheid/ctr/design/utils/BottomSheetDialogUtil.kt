package nl.rijksoverheid.ctr.design.utils

import android.widget.TextView
import androidx.fragment.app.FragmentManager
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget

data class ExpandedBottomSheetData(val title: String, val description: (HtmlTextViewWidget) -> Unit, val footer: (TextView) -> Unit)

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface BottomSheetDialogUtil {
    fun present(
        fragmentManager: FragmentManager,
        data: ExpandedBottomSheetData,
    )
}

class BottomSheetDialogUtilImpl: BottomSheetDialogUtil {
    override fun present(
        fragmentManager: FragmentManager,
        data: ExpandedBottomSheetData,
    ) {
        val bottomSheet = ExpandedBottomSheetDialogFragment(data)
        bottomSheet.show(fragmentManager, "bottomSheetTag")
    }
}