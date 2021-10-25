package nl.rijksoverheid.ctr.design.utils

import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.fragment.app.FragmentManager
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget

sealed class BottomSheetData(open val title: String, open val applyOnDescription: (HtmlTextViewWidget) -> Unit): Parcelable {
    @Parcelize class TitleDescription(override val title: String, override val applyOnDescription: (HtmlTextViewWidget) -> Unit): BottomSheetData(title, applyOnDescription)
    @Parcelize class TitleDescriptionWithButton(override val title: String, override val applyOnDescription: (HtmlTextViewWidget) -> Unit, val applyOnButton: (Button) -> Unit): BottomSheetData(title, applyOnDescription)
    @Parcelize class TitleDescriptionWithFooter(override val title: String, override val applyOnDescription: (HtmlTextViewWidget) -> Unit, val footerText: String): BottomSheetData(title, applyOnDescription)
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
        val bottomSheet = ExpandedBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ExpandedBottomSheetDialogFragment.dataKey, data)
            }
        }
        bottomSheet.show(fragmentManager, "bottomSheetTag")
    }
}
