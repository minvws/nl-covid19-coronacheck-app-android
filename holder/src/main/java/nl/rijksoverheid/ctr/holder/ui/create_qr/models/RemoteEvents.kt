package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEvents(
    val events: List<Event>,
    val protocolVersion: String,
    val providerIdentifier: String,
    val status: Status,
    val holder: Holder
) : Parcelable {

    enum class Status(val apiStatus: String) {
        UNKNOWN(""),
        PENDING("pending"),
        COMPLETE("complete");

        companion object {
            fun fromValue(value: String?): Status {
                return values().firstOrNull { it.apiStatus == value } ?: UNKNOWN
            }
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Holder(
        val infix: String?,
        val firstName: String?,
        val lastName: String?,
        val birthDate: String?
    ) : Parcelable

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Event(
        val type: String,
        val unique: String,
        val vaccination: Vaccination?
    ) : Parcelable {

        @Parcelize
        @JsonClass(generateAdapter = true)
        data class Vaccination(
            val date: LocalDate,
            val hpkCode: String,
            val type: String,
            val brand: String,
            val completedByMedicalStatement: String?,
            val doseNumber: String?,
            val totalDoses: String?,
            val country: String,
            val manufacturer: String
        ) : Parcelable

        fun getDate(): LocalDate {
            if (type == "vaccination") {
                return vaccination!!.date
            } else {
                // TODO Parse dates for other types
                return LocalDate.now()
            }
        }
    }
}

