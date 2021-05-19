package nl.rijksoverheid.ctr.holder.ui.create_qr.models

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
data class RemoteAccessTokens(
    val tokens: List<Token> = listOf()
) {
    @JsonClass(generateAdapter = true)
    data class Token(
        @Json(name = "provider_identifier") val providerIdentifier: String,
        @Json(name = "unomi") val unomi: String,
        @Json(name = "event") val event: String
    )
}
