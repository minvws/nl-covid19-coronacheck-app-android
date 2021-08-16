package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventVaccination(
    override val type: String?,
    val unique: String?,
    @Json(name = "vaccination") val vaccination: Vaccination?
) : Parcelable, RemoteEvent(type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Vaccination(
        val date: LocalDate?,
        val hpkCode: String?,
        val type: String?,
        val brand: String?,
        val completedByMedicalStatement: Boolean?,
        val completedByPersonalStatement: Boolean?,
        val completionReason: String?,
        val doseNumber: String?,
        val totalDoses: String?,
        val country: String?,
        val manufacturer: String?
    ) : Parcelable

    override fun getDate(): OffsetDateTime? {
        return vaccination?.date?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
    }

    override fun equals(other: Any?): Boolean {
        val otherVaccination = (other as? RemoteEventVaccination)?.vaccination ?: return false
        return this.vaccination?.date == otherVaccination.date &&
                ((this.vaccination?.hpkCode != null && otherVaccination.hpkCode != null && this.vaccination.hpkCode == otherVaccination.hpkCode) ||
                        (this.vaccination?.manufacturer != null && otherVaccination.manufacturer != null && this.vaccination.manufacturer == otherVaccination.manufacturer))
    }

    override fun hashCode(): Int {
        return vaccination?.date.hashCode()
    }
}