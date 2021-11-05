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
class VerifierConfig(
    @Json(name = "androidMinimumVersion") val verifierMinimumVersion: Int,
    @Json(name = "androidMinimumVersionMessage") val verifierMinimumVersionMessage: String,
    @Json(name = "playStoreURL") val playStoreURL: String,
    @Json(name = "appDeactivated") val verifierAppDeactivated: Boolean,
    @Json(name = "configTTL") val configTTL: Int,
    @Json(name = "configMinimumIntervalSeconds") val configMinimumInterval: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int,
    @Json(name = "informationURL") val verifierInformationURL: String,
    @Json(name = "androidRecommendedVersion") val verifierRecommendedVersion: Int,
    @Json(name = "upgradeRecommendationInterval") val upgradeRecommendationIntervalHours: Int,
    @Json(name = "universalLinkDomains") val verifierDeeplinkDomains: List<Url>,
    @Json(name = "clockDeviationThresholdSeconds") val verifierClockDeviationThresholdSeconds: Int,
    @Json(name = "configAlmostOutOfDateWarningSeconds") val verifierConfigAlmostOutOfDateWarningSeconds : Int
) : AppConfig(
    verifierAppDeactivated,
    verifierInformationURL,
    verifierMinimumVersion,
    configTTL,
    configMinimumInterval,
    emptyList(),
    verifierRecommendedVersion,
    upgradeRecommendationIntervalHours,
    verifierDeeplinkDomains,
    verifierClockDeviationThresholdSeconds,
    verifierConfigAlmostOutOfDateWarningSeconds
) {
    companion object {
        fun default(
            verifierMinimumVersion: Int = 1275,
            verifierMinimumVersionMessage: String = "Om de app te gebruiken heb je de laatste versie uit de store nodig.",
            playStoreURL: String = "https://play.google.com/store/apps/details?id=nl.rijksoverheid.ctr.verifier",
            verifierAppDeactivated: Boolean = false,
            configTTL: Int = 3600,
            configMinimumInterval: Int = 3600,
            maxValidityHours: Int = 40,
            verifierInformationURL: String = "https://coronacheck.nl",
            verifierRecommendedVersion: Int = 1275,
            upgradeRecommendationIntervalHours: Int = 24,
            returnApps: List<Url> = listOf(),
            verifierClockDeviationThresholdSeconds: Int = 30,
            verifierConfigAlmostOutOfDateWarningSeconds: Int = 300
        ) = VerifierConfig(
            verifierMinimumVersion = verifierMinimumVersion,
            verifierMinimumVersionMessage = verifierMinimumVersionMessage,
            playStoreURL = playStoreURL,
            verifierAppDeactivated = verifierAppDeactivated,
            configTTL = configTTL,
            configMinimumInterval = configMinimumInterval,
            maxValidityHours = maxValidityHours,
            verifierInformationURL = verifierInformationURL,
            verifierRecommendedVersion = verifierRecommendedVersion,
            upgradeRecommendationIntervalHours = upgradeRecommendationIntervalHours,
            verifierDeeplinkDomains = returnApps,
            verifierClockDeviationThresholdSeconds = verifierClockDeviationThresholdSeconds,
            verifierConfigAlmostOutOfDateWarningSeconds = verifierConfigAlmostOutOfDateWarningSeconds
        )
    }
}

