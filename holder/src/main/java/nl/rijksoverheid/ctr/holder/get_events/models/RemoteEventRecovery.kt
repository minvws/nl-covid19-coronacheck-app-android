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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventRecovery(
    override val type: String?,
    override val unique: String,
    val isSpecimen: Boolean,
    @Json(name = "recovery") val recovery: Recovery?
) : Parcelable, RemoteEvent(unique, type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Recovery(
        val sampleDate: LocalDate?,
        val validFrom: LocalDate?,
        val validUntil: LocalDate?,
        val country: String?
    ) : Parcelable

    override fun getDate(): OffsetDateTime? {
        return recovery?.sampleDate?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
    }
}
