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
    @Json(name = "androidMinimumVersion") val minimumVersion: Int = 1025,
    @Json(name = "appDeactivated") val appDeactivated: Boolean = false,
    @Json(name = "informationURL") val informationURL: String = "https://coronacheck.nl",
    @Json(name = "requireUpdateBefore") val requireUpdateBefore: Int = 1620781181,
    @Json(name = "temporarilyDisabled") val temporarilyDisabled: Boolean = false,
    @Json(name = "recoveryEventValidity") val recoveryEventValidity: Int = 7300,
    @Json(name = "testEventValidity") val testEventValidity: Int = 40,
    @Json(name = "domesticCredentialValidity") val domesticCredentialValidity: Int = 24,
    @Json(name = "credentialRenewalDays") val credentialRenewalDays: Int = 5,
    @Json(name = "configTTL") val configTtlSeconds: Int = 259200,
    @Json(name = "maxValidityHours") val maxValidityHours: Int = 40,
    @Json(name = "vaccinationEventValidity") val vaccinationEventValidity: Int = 14600,
    @Json(name = "euLaunchDate") val euLaunchDate: String = "2021-07-01T00:00:00Z",
    @Json(name = "hpkCodes") val hpkCodes: List<Code> = listOf(),
    @Json(name = "euBrands") val euBrands: List<Code> = listOf(),
    @Json(name = "euVaccinations") val euVaccinations: List<Code> = listOf(),
    @Json(name = "euManufacturers") val euManufacturers: List<Code> = listOf(),
    @Json(name = "euTestTypes") val euTestTypes: List<Code> = listOf(),
    @Json(name = "euTestManufacturers") val euTestManufacturers: List<Code> = listOf(),
    @Json(name = "nlTestTypes") val nlTestTypes: List<Code> = listOf(),
    @Json(name = "providerIdentifiers") val providerIdentifiers: List<Code> = listOf(),
    @Json(name = "ggdEnabled") val ggdEnabled: Boolean = true,
) : JSON() {

    @JsonClass(generateAdapter = true)
    data class Code(val code: String, val name: String): JSON()
}
