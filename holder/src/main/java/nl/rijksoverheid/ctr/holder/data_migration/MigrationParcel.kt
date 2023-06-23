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

@JsonClass(generateAdapter = true)
data class MigrationParcel(
    @Json(name = "i") val index: Int,
    @Json(name = "n") val numberOfPackages: Int,
    @Json(name = "p") val payload: String,
    @Json(name = "v") val version: String
)
