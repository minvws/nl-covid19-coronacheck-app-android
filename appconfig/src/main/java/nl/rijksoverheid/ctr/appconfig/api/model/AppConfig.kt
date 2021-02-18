/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api.model

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
data class AppConfig(
    @Json(name = "androidMinimumVersion") val minimumVersion: Int = 0,
    @Json(name = "androidMinimumVersionMessage") val message: String? = null,
    @Json(name = "playStoreURL") val playStoreURL: String? = null,
    @Json(name = "appDeactivated") val appDeactivated: Boolean = false,
    @Json(name = "informationURL") val informationURL: String? = null
)