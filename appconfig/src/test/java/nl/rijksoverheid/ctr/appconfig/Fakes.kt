package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun fakeAppConfigPersistenceManager(
    publicKeysJson: String? = null,
    appConfigJson: String? = null,
    lastFetchedSeconds: Long = 0L
) = object : AppConfigPersistenceManager {
    override fun savePublicKeysJson(json: String) {

    }

    override fun getPublicKeysJson(): String? {
        return publicKeysJson
    }

    override fun saveAppConfigJson(json: String) {

    }

    override fun getAppConfigJson(): String? {
        return appConfigJson
    }

    override fun getAppConfigLastFetchedSeconds(): Long {
        return lastFetchedSeconds
    }

    override fun saveAppConfigLastFetchedSeconds(seconds: Long) {

    }

}

fun fakeCachedAppConfigUseCase(
    appConfig: AppConfig? = fakeAppConfig(),
    publicKeys: PublicKeys = PublicKeys(listOf()),
    cachedAppConfigMaxValidityHours: Int = 0,
) = object : CachedAppConfigUseCase {
    override fun persistAppConfig(appConfig: AppConfig) {

    }

    override fun getCachedAppConfig(): AppConfig? {
        return appConfig
    }

    override fun getCachedAppConfigMaxValidityHours(): Int {
        return cachedAppConfigMaxValidityHours
    }

    override fun getCachedAppConfigVaccinationEventValidity(): Int {
        return appConfig.vaccinationEventValidity
    }

    override fun persistPublicKeys(publicKeys: PublicKeys) {

    }

    override fun getCachedPublicKeys(): PublicKeys? {
        return publicKeys
    }
}

fun fakeAppConfig(
    minimumVersion: Int = 1,
    appDeactivated: Boolean = false,
    informationURL: String = "",
    configTtlSeconds: Int = 0,
    maxValidityHours: Int = 0
) = AppConfig(
    minimumVersion = minimumVersion,
    appDeactivated = appDeactivated,
    informationURL = informationURL,
    configTtlSeconds = configTtlSeconds,
    maxValidityHours = maxValidityHours
)
