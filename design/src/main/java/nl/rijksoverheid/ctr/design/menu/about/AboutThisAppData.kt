package nl.rijksoverheid.ctr.design.menu.about

import android.os.Parcelable
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
    val readMoreItems: List<AboutThisAppItem> = listOf(),
    val configVersionHash: String,
    val configVersionTimestamp: Long
) : Parcelable {

    sealed class AboutThisAppItem(open val text: String) : Parcelable

    @Parcelize
    data class Url(override val text: String, val url: String) : AboutThisAppItem(text)

    @Parcelize
    data class ClearAppData(override val text: String) : AboutThisAppItem(text)
}
