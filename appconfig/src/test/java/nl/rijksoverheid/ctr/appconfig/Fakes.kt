package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import java.security.MessageDigest

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
    appConfig: AppConfig = fakeAppConfig(),
) = object : CachedAppConfigUseCase {

    override fun isCachedAppConfigValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCachedAppConfig(): AppConfig {
        return appConfig
    }

    override fun getCachedAppConfigHash(): String {
        val bytes = getCachedAppConfig().toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        // Return first 7 characters of hash
        return digest.fold("", { str, it -> str + "%02x".format(it) }).subSequence(0,7).toString()
    }
}

fun fakeAppConfig(
    minimumVersion: Int = 1,
    appDeactivated: Boolean = false,
    informationURL: String = "",
    configTtlSeconds: Int = 0,
    maxValidityHours: Int = 0
) = HolderConfig.default(
    holderMinimumVersion = minimumVersion,
    holderAppDeactivated = appDeactivated,
    holderInformationURL = informationURL,
    configTTL = configTtlSeconds,
    maxValidityHours = maxValidityHours,
    euLaunchDate = "",
    credentialRenewalDays = 0,
    domesticCredentialValidity = 0,
    testEventValidityHours = 0,
    recoveryEventValidityDays = 0,
    temporarilyDisabled = false,
    requireUpdateBefore = 0,
    ggdEnabled = true
)
