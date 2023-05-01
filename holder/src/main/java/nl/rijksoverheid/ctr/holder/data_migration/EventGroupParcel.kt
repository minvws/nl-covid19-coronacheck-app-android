/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.data_migration

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

@JsonClass(generateAdapter = true)
data class EventGroupParcel(
    @Json(name = "e") val expiryDate: OffsetDateTime?,
    @Json(name = "p") val providerIdentifier: String,
    @Json(name = "t") val type: OriginType,
    @Json(name = "d") val jsonData: ByteArray
)
