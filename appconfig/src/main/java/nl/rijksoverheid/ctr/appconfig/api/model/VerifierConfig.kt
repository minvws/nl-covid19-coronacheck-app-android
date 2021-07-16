package nl.rijksoverheid.ctr.appconfig.api.model

import com.squareup.moshi.Json


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierConfig(
    @Json(name = "androidMinimumVersion") val verifierMinimumVersion: Int,
    @Json(name = "androidMinimumVersionMessage") val verifierMinimumVersionMessage: String,
    @Json(name = "playStoreURL") val playStoreURL: String,
    @Json(name = "appDeactivated") val verifierAppDeactivated: Boolean,
    @Json(name = "configTTL") val configTTL: Int,
    @Json(name = "maxValidityHours") val maxValidityHours: Int,
    @Json(name = "informationURL") val verifierInformationURL: String,
): AppConfig(verifierAppDeactivated, verifierInformationURL, verifierMinimumVersion, configTTL, emptyList()) {
    companion object {
        fun default(
            verifierMinimumVersion: Int = 1275,
            verifierMinimumVersionMessage: String = "Om de app te gebruiken heb je de laatste versie uit de store nodig.",
            playStoreURL: String = "https://play.google.com/store/apps/details?id=nl.rijksoverheid.ctr.verifier",
            verifierAppDeactivated: Boolean = false,
            configTTL: Int = 3600,
            maxValidityHours: Int = 40,
            verifierInformationURL: String = "https://coronacheck.nl",
        ) = VerifierConfig(
            verifierMinimumVersion = verifierMinimumVersion,
            verifierMinimumVersionMessage = verifierMinimumVersionMessage,
            playStoreURL = playStoreURL, 
            verifierAppDeactivated = verifierAppDeactivated, 
            configTTL = configTTL, 
            maxValidityHours = maxValidityHours, 
            verifierInformationURL = verifierInformationURL,
        )
    }
}

