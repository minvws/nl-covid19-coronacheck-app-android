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
    @Json(name = "requireUpdateBefore") val requireUpdateBefore: Int,
    @Json(name = "temporarilyDisabled") val temporarilyDisabled: Boolean,
    @Json(name = "recoveryEventValidity") val recoveryEventValidity: Int,
    @Json(name = "testEventValidity") val testEventValidity: Int,
    @Json(name = "domesticCredentialValidity") val domesticCredentialValidity: Int,
    @Json(name = "credentialRenewalDays") val credentialRenewalDays: Int,
    @Json(name = "configTTL") val configTtlSeconds: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int,
    @Json(name = "vaccinationEventValidity") val vaccinationEventValidity: Int,
    @Json(name = "euLaunchDate") val euLaunchDate: String,
    @Json(name = "hpkCodes") val hpkCodes: List<Code>,
    @Json(name = "euBrands") val euBrands: List<Code>,
    @Json(name = "euVaccinations") val euVaccinations: List<Code>,
    @Json(name = "euManufacturers") val euManufacturers: List<Code>,
    @Json(name = "euTestTypes") val euTestTypes: List<Code>,
    @Json(name = "euTestManufacturers") val euTestManufacturers: List<Code>,
    @Json(name = "nlTestTypes") val nlTestTypes: List<Code>,
    @Json(name = "providerIdentifiers") val providerIdentifiers: List<Code>,
    @Json(name = "ggdEnabled") val ggdEnabled: Boolean,
) : JSON() {

    @JsonClass(generateAdapter = true)
    data class Code(val code: String, val name: String): JSON()

    companion object {
        fun default(
            minimumVersion: Int = 1025,
            appDeactivated: Boolean = false,
            informationURL: String = "https://coronacheck.nl",
            requireUpdateBefore: Int = 1620781181,
            temporarilyDisabled: Boolean = false,
            recoveryEventValidity: Int = 7300,
            testEventValidity: Int = 40,
            domesticCredentialValidity: Int = 24,
            credentialRenewalDays: Int = 5,
            configTtlSeconds: Int = 259200,
            maxValidityHours: Int = 40,
            vaccinationEventValidity: Int = 14600,
            euLaunchDate: String = "2021-07-01T00:00:00Z",
            hpkCodes: List<Code> = listOf(),
            euBrands: List<Code> = listOf(),
            euVaccinations: List<Code> = listOf(),
            euManufacturers: List<Code> = listOf(),
            euTestTypes: List<Code> = listOf(),
            euTestManufacturers: List<Code> = listOf(),
            nlTestTypes: List<Code> = listOf(),
            providerIdentifiers: List<Code> = listOf(),
            ggdEnabled: Boolean = true,
        ) = AppConfig(
            minimumVersion = minimumVersion,
            appDeactivated = appDeactivated,
            informationURL = informationURL,
            requireUpdateBefore = requireUpdateBefore,
            temporarilyDisabled = temporarilyDisabled,
            recoveryEventValidity = recoveryEventValidity,
            testEventValidity = testEventValidity,
            domesticCredentialValidity = domesticCredentialValidity,
            credentialRenewalDays = credentialRenewalDays,
            configTtlSeconds = configTtlSeconds,
            maxValidityHours = maxValidityHours,
            vaccinationEventValidity = vaccinationEventValidity,
            euLaunchDate = euLaunchDate,
            hpkCodes = hpkCodes,
            euBrands = euBrands,
            euVaccinations = euVaccinations,
            euManufacturers = euManufacturers,
            euTestTypes = euTestTypes,
            euTestManufacturers = euTestManufacturers,
            nlTestTypes = nlTestTypes,
            providerIdentifiers = providerIdentifiers,
            ggdEnabled = ggdEnabled,
        )
    }
}
