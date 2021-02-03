package nl.rijksoverheid.ctr.shared.models

import com.squareup.moshi.Json

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class Config(
    @Json(name = "androidMinimumVersion") val minimumVersion: Int,
    @Json(name = "androidMinimumVersionMessage") val message: String,
    @Json(name = "playStoreURL") val playStoreURL: String,
    @Json(name = "appDeactivated") val appDeactivated: Boolean,
    @Json(name = "informationURL") val informationURL: String
)
