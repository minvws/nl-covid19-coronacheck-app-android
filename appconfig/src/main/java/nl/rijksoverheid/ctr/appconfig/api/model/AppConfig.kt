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
    @Json(name = "requireUpdateBefore") val requireUpdateBefore: Int = 1620781181,
    @Json(name = "temporarilyDisabled") val temporarilyDisabled: Boolean = false,
    @Json(name = "recoveryEventValidity") val recoveryEventValidity: Int = 14600,
    @Json(name = "testEventValidity") val testEventValidity: Int = 7300,
    @Json(name = "domesticCredentialValidity") val domesticCredentialValidity: Int = 40,
    @Json(name = "credentialRenewalDays") val credentialRenewalDays: Int = 24,
    @Json(name = "configTTL") val configTtlSeconds: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int,
    @Json(name = "vaccinationEventValidity") val vaccinationEventValidity: Int = 14600,
    @Json(name = "euLaunchDate") val euLaunchDate: String = "2021-07-01",
    @Json(name = "hpkCodes") val hpkCodes: List<Code> = listOf(),
    @Json(name = "euBrands") val euBrands: List<Code> = listOf(),
    @Json(name = "euVaccinations") val euVaccinations: List<Code> = listOf(),
    @Json(name = "euManufacturers") val euManufacturers: List<Code> = listOf(),
    @Json(name = "euTestTypes") val euTestTypes: List<Code> = listOf(),
    @Json(name = "euTestManufacturers") val euTestManufacturers: List<Code> = listOf()
) : JSON() {

    @JsonClass(generateAdapter = true)
    data class Code(val code: String, val name: String): JSON()
}
