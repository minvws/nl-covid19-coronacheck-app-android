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
import nl.rijksoverheid.ctr.shared.models.JSON

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class AppConfig(
    @Json(name = "androidMinimumVersion") val minimumVersion: Int,
    @Json(name = "appDeactivated") val appDeactivated: Boolean,
    @Json(name = "informationURL") val informationURL: String,
    @Json(name = "configTTL") val configTtlSeconds: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int
) : JSON()
