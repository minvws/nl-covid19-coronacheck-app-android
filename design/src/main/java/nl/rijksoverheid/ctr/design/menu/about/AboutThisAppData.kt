package nl.rijksoverheid.ctr.design.menu.about

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class AboutThisAppData(
    val sections: List<AboutThisAppSection> = listOf(),
    val resetAppDialogDirection: Destination? = null,
    val deeplinkScannerUrl: String? = null
) : Parcelable {

    @Parcelize
    data class AboutThisAppSection(@StringRes val header: Int, val items: List<AboutThisAppItem>) : Parcelable

    sealed class AboutThisAppItem(open val text: String) : Parcelable

    @Parcelize
    data class Destination(override val text: String, val destinationId: Int, val arguments: Bundle? = null) : AboutThisAppItem(text)

    @Parcelize
    data class Url(override val text: String, val url: String) : AboutThisAppItem(text)
}
