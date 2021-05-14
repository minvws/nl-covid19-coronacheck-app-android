package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import com.squareup.moshi.JsonClass

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class RemoteEvents(
    val events: List<Event>,
    val protocolVersion: String,
    val providerIdentifier: String,
    val status: Status,
    val holder: Holder
) {

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

    @JsonClass(generateAdapter = true)
    data class Holder(
        val identityHash: String?,
        val firstName: String?,
        val lastName: String?,
        val birthDate: String?
    )

    @JsonClass(generateAdapter = true)
    data class Event(
        val type: String,
        val unique: String,
        val vaccination: Vaccination
    ) {

        @JsonClass(generateAdapter = true)
        data class Vaccination(
            val date: String,
            val hpkCode: String,
            val type: String,
            val brand: String,
            val batchNumber: String,
            val administeringCenter: String,
            val country: String
        )
    }
}

