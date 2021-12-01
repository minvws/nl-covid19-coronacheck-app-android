package nl.rijksoverheid.ctr.design.menu.about

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
    val versionName: String,
    val versionCode: String,
    val sections: List<AboutThisAppSection> = listOf(),
    val configVersionHash: String,
    val configVersionTimestamp: Long
) : Parcelable {

    @Parcelize
    data class AboutThisAppSection(@StringRes val header: Int, val items: List<AboutThisAppItem>): Parcelable

    sealed class AboutThisAppItem(open val text: String) : Parcelable

    @Parcelize
    data class Url(override val text: String, val url: String) : AboutThisAppItem(text)

    @Parcelize
    data class ClearAppData(override val text: String) : AboutThisAppItem(text)
}
