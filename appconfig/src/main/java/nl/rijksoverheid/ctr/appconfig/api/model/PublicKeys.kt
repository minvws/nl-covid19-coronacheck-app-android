package nl.rijksoverheid.ctr.appconfig.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.shared.models.JSON

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class PublicKeys(
    @Json(name = "cl_keys") val clKeys: List<ClKey>,
) : JSON() {
    @JsonClass(generateAdapter = true)
    data class ClKey(
        val id: String,
        @Json(name = "public_key") val publicKey: String
    )
}
