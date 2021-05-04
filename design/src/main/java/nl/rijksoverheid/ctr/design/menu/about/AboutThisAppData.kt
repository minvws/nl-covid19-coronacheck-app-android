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
    val readMoreItems: List<ReadMoreItem> = listOf()
) : Parcelable {

    @Parcelize
    data class ReadMoreItem(val text: String, val url: String) : Parcelable
}
