package nl.rijksoverheid.ctr.citizen.models

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
data class CustomerQR(
    @Json(name = "public_key") val publicKey: String,
    @Json(name = "nonce") val nonce: String,
    @Json(name = "payload") val payload: String
)
