package nl.rijksoverheid.ctr.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class EventQR(
    @Json(name = "event_signature") val eventSignature: String,
    val event: Event
) {

    @JsonClass(generateAdapter = true)
    data class Event(
        val name: String,
        val uuid: String,
        @Json(name = "public_key") val publicKey: String,
        @Json(name = "valid_from") val validFrom: Long,
        @Json(name = "valid_to") val validTo: Long,
        val location: Location,
        val type: Type,
        @Json(name = "valid_tests") val validTests: List<ValidTests>
    ) {

        @JsonClass(generateAdapter = true)
        data class Location(
            val uuid: String,
            val name: String,
            @Json(name = "street_name") val streetName: String,
            @Json(name = "house_number") val houseNumber: Int,
            val zipcode: String
        )

        @JsonClass(generateAdapter = true)
        data class Type(
            val name: String,
            val uuid: String
        )

        @JsonClass(generateAdapter = true)
        data class ValidTests(
            val name: String,
            val uuid: String,
            @Json(name = "max_validity") val maxValdity: Long
        )
    }
}
