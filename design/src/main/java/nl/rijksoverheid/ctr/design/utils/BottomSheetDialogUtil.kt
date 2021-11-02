package nl.rijksoverheid.ctr.design.utils

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.fragment.app.FragmentManager
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget

@Parcelize class DescriptionData(val htmlText: Int? = null, val htmlTextString: String? = null, val htmlLinksEnabled: Boolean = false, val customLinkIntent: Intent? = null): Parcelable
@Parcelize class ButtonData(val text: String, val link: String): Parcelable

sealed class BottomSheetData(open val title: String, open val descriptionData: DescriptionData): Parcelable {
    @Parcelize class TitleDescription(override val title: String, override val descriptionData: DescriptionData): BottomSheetData(title, descriptionData)
    @Parcelize class TitleDescriptionWithButton(override val title: String, override val descriptionData: DescriptionData, val buttonData: ButtonData): BottomSheetData(title, descriptionData)
    @Parcelize class TitleDescriptionWithFooter(override val title: String, override val descriptionData: DescriptionData, val footerText: String): BottomSheetData(title, descriptionData)
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
