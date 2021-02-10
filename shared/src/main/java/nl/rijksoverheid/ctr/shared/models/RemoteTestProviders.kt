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
data class RemoteTestProviders(
    @Json(name = "corona_test_providers") val providers: List<Provider>
) {

    @JsonClass(generateAdapter = true)
    data class Provider(
        val name: String,
        @Json(name = "provider_identifier") val providerIdentifier: String,
        @Json(name = "result_url") val resultUrl: String,
        @Json(name = "public_key") val publicKey: String
    )
}
