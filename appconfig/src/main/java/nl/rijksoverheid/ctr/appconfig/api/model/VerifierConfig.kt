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
    @Json(name = "configAlmostOutOfDateWarningSeconds") val verifierConfigAlmostOutOfDateWarningSeconds: Int,
    @Json(name = "scanLockWarningSeconds") val scanLockWarningSeconds: Int,
    @Json(name = "scanLockSeconds") val scanLockSeconds: Int,
    @Json(name = "scanLogStorageSeconds") val scanLogStorageSeconds: Int,
    @Json(name = "androidEnableVerificationPolicyVersion") val verifierEnableVerificationPolicyVersion: Int = 0,
    @Json(name = "verificationPolicies") val verificationPolicies: List<String>,
    @Json(name = "contactInformation") val contactInformation: ContactInformation =
    ContactInformation("08001421", "+31 70 750 37 20", 1, "08:00", 5, "18:00")
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
    verifierConfigAlmostOutOfDateWarningSeconds,
    contactInformation
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
            verifierConfigAlmostOutOfDateWarningSeconds: Int = 300,
            scanLockWarningSeconds: Int = 3600,
            scanLockSeconds: Int = 300,
            scanLogStorageSeconds: Int = 3600,
            verifierEnableVerificationPolicyVersion: Int = 0,
            policiesEnabled: List<String> = listOf("3G")
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
            verifierConfigAlmostOutOfDateWarningSeconds = verifierConfigAlmostOutOfDateWarningSeconds,
            scanLockWarningSeconds = scanLockWarningSeconds,
            scanLockSeconds = scanLockSeconds,
            scanLogStorageSeconds = scanLogStorageSeconds,
            verifierEnableVerificationPolicyVersion = verifierEnableVerificationPolicyVersion,
            verificationPolicies = policiesEnabled
        )
    }
}
