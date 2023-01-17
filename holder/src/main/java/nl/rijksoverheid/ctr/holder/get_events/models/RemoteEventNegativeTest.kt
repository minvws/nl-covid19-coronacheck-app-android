/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventNegativeTest(
    override val type: String?,
    override val unique: String?,
    val isSpecimen: Boolean?,
    @Json(name = "negativetest") val negativeTest: NegativeTest?
) : Parcelable, RemoteEvent(unique, type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class NegativeTest(
        val sampleDate: OffsetDateTime?,
        val negativeResult: Boolean?,
        val facility: String?,
        val type: String?,
        val name: String?,
        val country: String?,
        val manufacturer: String?
    ) : Parcelable

    override fun getDate(): OffsetDateTime? {
        return negativeTest?.sampleDate
    }
}
