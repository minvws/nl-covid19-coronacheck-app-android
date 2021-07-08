package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource

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

    override fun getAppConfigLastFetchedSeconds(): Long {
        return lastFetchedSeconds
    }

    override fun saveAppConfigLastFetchedSeconds(seconds: Long) {

    }

}

fun fakeCachedAppConfigUseCase(
    appConfig: AppConfig? = fakeAppConfig(),
    publicKeys: BufferedSource = "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType()).source(),
    cachedAppConfigMaxValidityHours: Int = 0,
    cachedAppConfigVaccinationEventValidity: Int = 0
) = object : CachedAppConfigUseCase {

    override fun getCachedAppConfig(): AppConfig? {
        return appConfig
    }

    override fun getCachedAppConfigRecoveryEventValidity(): Int {
        return appConfig?.recoveryEventValidity ?: 0
    }

    override fun getCachedAppConfigMaxValidityHours(): Int {
        return cachedAppConfigMaxValidityHours
    }

    override fun getCachedAppConfigVaccinationEventValidity(): Int {
        return cachedAppConfigVaccinationEventValidity
    }

    override fun getCachedPublicKeys(): BufferedSource {
        return publicKeys
    }

    override fun getProviderName(providerIdentifier: String?): String {
        return ""
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
    maxValidityHours = maxValidityHours,
    euLaunchDate = "",
    credentialRenewalDays = 0,
    domesticCredentialValidity = 0,
    testEventValidity = 0,
    recoveryEventValidity = 0,
    temporarilyDisabled = false,
    requireUpdateBefore = 0
)
