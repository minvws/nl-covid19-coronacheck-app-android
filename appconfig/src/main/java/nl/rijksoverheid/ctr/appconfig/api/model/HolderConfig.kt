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
data class HolderConfig(
    @Json(name = "androidMinimumVersion") val holderMinimumVersion: Int,
    @Json(name = "appDeactivated") val holderAppDeactivated: Boolean,
    @Json(name = "informationURL") val holderInformationURL: String,
    @Json(name = "requireUpdateBefore") val requireUpdateBefore: Int,
    @Json(name = "temporarilyDisabled") val temporarilyDisabled: Boolean,
    @Json(name = "recoveryEventValidity") val recoveryEventValidity: Int,
    @Json(name = "testEventValidity") val testEventValidity: Int,
    @Json(name = "domesticCredentialValidity") val domesticCredentialValidity: Int,
    @Json(name = "credentialRenewalDays") val credentialRenewalDays: Int,
    @Json(name = "configTTL") val configTTL: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int,
    @Json(name = "vaccinationEventValidity") val vaccinationEventValidity: Int,
    @Json(name = "euLaunchDate") val euLaunchDate: String,
    @Json(name = "hpkCodes") val hpkCodes: List<HpkCode>,
    @Json(name = "euBrands") val euBrands: List<Code>,
    @Json(name = "euVaccinations") val euVaccinations: List<Code>,
    @Json(name = "euManufacturers") val euManufacturers: List<Code>,
    @Json(name = "euTestTypes") val euTestTypes: List<Code>,
    @Json(name = "euTestManufacturers") val euTestManufacturers: List<Code>,
    @Json(name = "nlTestTypes") val nlTestTypes: List<Code>,
    @Json(name = "providerIdentifiers") val providerIdentifiers: List<Code>,
    @Json(name = "ggdEnabled") val ggdEnabled: Boolean,
    @Json(name = "universalLinkDomains") val deeplinkDomains: List<Url>,
    @Json(name = "domesticQRRefreshSeconds") val domesticQRRefreshSeconds: Int,
    @Json(name = "clockDeviationThresholdSeconds") val holderClockDeviationThresholdSeconds: Int,
    @Json(name = "androidRecommendedVersion") val holderRecommendedVersion: Int,
    @Json(name = "upgradeRecommendationInterval") val upgradeRecommendationIntervalHours: Int,
    @Json(name = "luhnCheckEnabled") val luhnCheckEnabled: Boolean,
) : AppConfig(
    holderAppDeactivated,
    holderInformationURL,
    holderMinimumVersion,
    configTTL,
    providerIdentifiers,
    holderRecommendedVersion,
    upgradeRecommendationIntervalHours
) {

    companion object {
        fun default(
            holderMinimumVersion: Int = 1025,
            holderAppDeactivated: Boolean = false,
            holderInformationURL: String = "https://coronacheck.nl",
            requireUpdateBefore: Int = 1620781181,
            temporarilyDisabled: Boolean = false,
            recoveryEventValidity: Int = 7300,
            testEventValidity: Int = 40,
            domesticCredentialValidity: Int = 24,
            credentialRenewalDays: Int = 5,
            configTTL: Int = 259200,
            maxValidityHours: Int = 40,
            vaccinationEventValidity: Int = 14600,
            euLaunchDate: String = "2021-07-01T00:00:00Z",
            hpkCodes: List<HpkCode> = listOf(),
            euBrands: List<Code> = listOf(),
            euVaccinations: List<Code> = listOf(),
            euManufacturers: List<Code> = listOf(),
            euTestTypes: List<Code> = listOf(),
            euTestManufacturers: List<Code> = listOf(),
            nlTestTypes: List<Code> = listOf(),
            providerIdentifiers: List<Code> = listOf(),
            ggdEnabled: Boolean = true,
            returnApps: List<Url> = listOf(),
            domesticQRRefreshSeconds: Int = 10,
            holderClockDeviationThresholdSeconds: Int = 30,
            holderRecommendedVersion: Int = 1025,
            upgradeRecommendationIntervalHours: Int = 24,
            luhnCheckEnabled: Boolean = false,
        ) = HolderConfig(
            holderMinimumVersion = holderMinimumVersion,
            holderAppDeactivated = holderAppDeactivated,
            holderInformationURL = holderInformationURL,
            requireUpdateBefore = requireUpdateBefore,
            temporarilyDisabled = temporarilyDisabled,
            recoveryEventValidity = recoveryEventValidity,
            testEventValidity = testEventValidity,
            domesticCredentialValidity = domesticCredentialValidity,
            credentialRenewalDays = credentialRenewalDays,
            configTTL = configTTL,
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
            deeplinkDomains = returnApps,
            domesticQRRefreshSeconds = domesticQRRefreshSeconds,
            holderClockDeviationThresholdSeconds = holderClockDeviationThresholdSeconds,
            holderRecommendedVersion = holderRecommendedVersion,
            upgradeRecommendationIntervalHours = upgradeRecommendationIntervalHours,
            luhnCheckEnabled = luhnCheckEnabled,
        )
    }
}

