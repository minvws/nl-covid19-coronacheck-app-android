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
data class AgentQR(
    val agent: Agent,
    @Json(name = "agent_signature") val agentSignature: String
) {

    @JsonClass(generateAdapter = true)
    data class Agent(
        val event: Event
    ) {

        @JsonClass(generateAdapter = true)
        data class Event(
            val name: String,
            @Json(name = "private_key") val privateKey: String,
            @Json(name = "valid_from") val validFrom: Long,
            @Json(name = "valid_to") val validTo: Long,
            @Json(name = "type") val type: Type,
            @Json(name = "valid_tests") val validTests: List<ValidTest>
        ) {

            @JsonClass(generateAdapter = true)
            data class Type(
                val name: String,
                val uuid: String
            )

            @JsonClass(generateAdapter = true)
            data class ValidTest(
                val name: String,
                val uuid: String,
                @Json(name = "max_validity") val maxValidity: Long
            )
        }
    }
}
