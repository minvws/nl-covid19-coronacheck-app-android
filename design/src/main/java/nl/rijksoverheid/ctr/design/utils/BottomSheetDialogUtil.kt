package nl.rijksoverheid.ctr.design.utils

import android.widget.Button
import androidx.fragment.app.FragmentManager
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget

sealed class BottomSheetData(open val title: String, open val applyOnDescription: (HtmlTextViewWidget) -> Unit) {
    class TitleDescription(override val title: String, override val applyOnDescription: (HtmlTextViewWidget) -> Unit): BottomSheetData(title, applyOnDescription)
    class TitleDescriptionWithButton(override val title: String, override val applyOnDescription: (HtmlTextViewWidget) -> Unit, val applyOnButton: (Button) -> Unit): BottomSheetData(title, applyOnDescription)
    class TitleDescriptionWithFooter(override val title: String, override val applyOnDescription: (HtmlTextViewWidget) -> Unit, val footerText: String): BottomSheetData(title, applyOnDescription)
}

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
        data: BottomSheetData,
    )
}

class BottomSheetDialogUtilImpl: BottomSheetDialogUtil {
    override fun present(
        fragmentManager: FragmentManager,
        data: BottomSheetData,
    ) {
        val bottomSheet = ExpandedBottomSheetDialogFragment(data)
        bottomSheet.show(fragmentManager, "bottomSheetTag")
    }
}
