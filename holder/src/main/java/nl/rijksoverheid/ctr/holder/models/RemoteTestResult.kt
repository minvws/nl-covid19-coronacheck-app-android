package nl.rijksoverheid.ctr.holder.models

import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class RemoteTestResult(
    val result: Result?,
    val protocolVersion: String,
    val providerIdentifier: String,
    val status: Status
) {

    enum class Status(val apiStatus: String) {
        UNKNOWN(""),
        PENDING("pending"),
        INVALID_TOKEN("invalid_token"),
        VERIFICATION_REQUIRED("verification_required"),
        COMPLETE("complete");

        companion object {
            fun fromValue(value: String?): Status {
                return values().firstOrNull { it.apiStatus == value } ?: UNKNOWN
            }
        }
    }

    @JsonClass(generateAdapter = true)
    data class Result(
        val unique: String,
        val sampleDate: OffsetDateTime,
        val testType: String,
        val negativeResult: Boolean,
        val holder: Holder
    ) {

        @JsonClass(generateAdapter = true)
        data class Holder(
            val firstNameInitial: String,
            val lastNameInitial: String,
            val birthDay: String,
            val birthMonth: String
        )
    }
}
