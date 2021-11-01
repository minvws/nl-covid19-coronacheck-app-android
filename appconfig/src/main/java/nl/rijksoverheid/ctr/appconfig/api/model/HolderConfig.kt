package nl.rijksoverheid.ctr.appconfig.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset


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
    @Json(name = "recoveryEventValidityDays") val recoveryEventValidityDays: Int,
    @Json(name = "testEventValidityHours") val testEventValidityHours: Int,
    @Json(name = "domesticCredentialValidity") val domesticCredentialValidity: Int,
    @Json(name = "credentialRenewalDays") val credentialRenewalDays: Int,
    @Json(name = "configTTL") val configTTL: Int,
    @Json(name = "configMinimumIntervalSeconds") val configMinimumInterval: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int,
    @Json(name = "vaccinationEventValidityDays") val vaccinationEventValidityDays: Int,
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
    @Json(name = "universalLinkDomains") val holderDeeplinkDomains: List<Url>,
    @Json(name = "domesticQRRefreshSeconds") val domesticQRRefreshSeconds: Int,
    @Json(name = "clockDeviationThresholdSeconds") val holderClockDeviationThresholdSeconds: Int,
    @Json(name = "androidRecommendedVersion") val holderRecommendedVersion: Int,
    @Json(name = "upgradeRecommendationInterval") val upgradeRecommendationIntervalHours: Int,
    @Json(name = "luhnCheckEnabled") val luhnCheckEnabled: Boolean,
    @Json(name = "internationalQRRelevancyDays") val internationalQRRelevancyDays: Int,
    @Json(name = "recoveryGreencardRevisedValidityLaunchDate") val recoveryGreenCardRevisedValidityLaunchDate: String,
    @Json(name = "configAlmostOutOfDateWarningSeconds") val holderConfigAlmostOutOfDateWarningSeconds : Int
) : AppConfig(
    holderAppDeactivated,
    holderInformationURL,
    holderMinimumVersion,
    configTTL,
    configMinimumInterval,
    providerIdentifiers,
    holderRecommendedVersion,
    upgradeRecommendationIntervalHours,
    holderDeeplinkDomains,
    holderClockDeviationThresholdSeconds,
    holderConfigAlmostOutOfDateWarningSeconds
) {

    companion object {
        fun default(
            holderMinimumVersion: Int = 1025,
            holderAppDeactivated: Boolean = false,
            holderInformationURL: String = "https://coronacheck.nl",
            requireUpdateBefore: Int = 1620781181,
            temporarilyDisabled: Boolean = false,
            recoveryEventValidityDays: Int = 365,
            testEventValidityHours: Int = 40,
            domesticCredentialValidity: Int = 24,
            credentialRenewalDays: Int = 5,
            configTTL: Int = 259200,
            configMinimumInterval: Int = 3600,
            maxValidityHours: Int = 40,
            vaccinationEventValidityDays: Int = 730,
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
            internationalQRRelevancyDays: Int = 28,
            holderConfigAlmostOutOfDateWarningSeconds: Int = 300
        ) = HolderConfig(
            holderMinimumVersion = holderMinimumVersion,
            holderAppDeactivated = holderAppDeactivated,
            holderInformationURL = holderInformationURL,
            requireUpdateBefore = requireUpdateBefore,
            temporarilyDisabled = temporarilyDisabled,
            recoveryEventValidityDays = recoveryEventValidityDays,
            testEventValidityHours = testEventValidityHours,
            domesticCredentialValidity = domesticCredentialValidity,
            credentialRenewalDays = credentialRenewalDays,
            configTTL = configTTL,
            configMinimumInterval = configMinimumInterval,
            maxValidityHours = maxValidityHours,
            vaccinationEventValidityDays = vaccinationEventValidityDays,
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
            holderDeeplinkDomains = returnApps,
            domesticQRRefreshSeconds = domesticQRRefreshSeconds,
            holderClockDeviationThresholdSeconds = holderClockDeviationThresholdSeconds,
            holderRecommendedVersion = holderRecommendedVersion,
            upgradeRecommendationIntervalHours = upgradeRecommendationIntervalHours,
            luhnCheckEnabled = luhnCheckEnabled,
            internationalQRRelevancyDays = internationalQRRelevancyDays,
            recoveryGreenCardRevisedValidityLaunchDate = "1970-01-01T00:00:00Z",
            holderConfigAlmostOutOfDateWarningSeconds = holderConfigAlmostOutOfDateWarningSeconds
        )
    }
}

