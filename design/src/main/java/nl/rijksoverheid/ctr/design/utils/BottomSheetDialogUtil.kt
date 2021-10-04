package nl.rijksoverheid.ctr.design.utils

import android.widget.Button
import androidx.fragment.app.FragmentManager
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget

sealed class BottomSheetData(open val title: String, open val description: (HtmlTextViewWidget) -> Unit) {
    class TitleDescription(override val title: String, override val description: (HtmlTextViewWidget) -> Unit): BottomSheetData(title, description)
    class TitleDescriptionWithButton(override val title: String, override val description: (HtmlTextViewWidget) -> Unit, val buttonCallback: (Button) -> Unit): BottomSheetData(title, description)
    class TitleDescriptionWithFooter(override val title: String, override val description: (HtmlTextViewWidget) -> Unit, val footerText: String): BottomSheetData(title, description)
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
