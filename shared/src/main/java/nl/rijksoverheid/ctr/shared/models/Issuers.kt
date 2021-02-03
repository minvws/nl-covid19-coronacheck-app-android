package nl.rijksoverheid.ctr.shared.models

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
data class Issuers(
    val issuers: List<Issuer>,
) {

    @JsonClass(generateAdapter = true)
    data class Issuer(
        val uuid: String,
        val name: String,
        @Json(name = "public_key") val publicKey: String
    )
}
