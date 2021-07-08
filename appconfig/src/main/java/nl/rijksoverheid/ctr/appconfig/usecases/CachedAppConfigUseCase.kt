package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import okio.BufferedSource
import java.io.File

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CachedAppConfigUseCase {
    fun getCachedAppConfig(): AppConfig?
    fun getCachedAppConfigRecoveryEventValidity(): Int
    fun getCachedAppConfigMaxValidityHours(): Int
    fun getCachedAppConfigVaccinationEventValidity(): Int
    fun getCachedPublicKeys(): BufferedSource?
    fun getProviderName(providerIdentifier: String?): String
}

class CachedAppConfigUseCaseImpl constructor(
    private val appConfigStorageManager: AppConfigStorageManager,
    private val filesDirPath: String,
    private val moshi: Moshi
) : CachedAppConfigUseCase {

    override fun getCachedAppConfig(): AppConfig? {
        val configFile = File(filesDirPath, "config.json")

        if (!configFile.exists()) {
            return AppConfig()
        }

        return try {
            appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()?.toObject(moshi)
        } catch (exc: Exception) {
            configFile.delete()
            AppConfig()
        }
    }

    override fun getCachedAppConfigRecoveryEventValidity(): Int {
        return getCachedAppConfig()?.recoveryEventValidity
            ?: throw IllegalStateException("AppConfig should be cached")
    }

    override fun getCachedAppConfigMaxValidityHours(): Int {
        return getCachedAppConfig()?.maxValidityHours
            ?: throw IllegalStateException("AppConfig should be cached")
    }

    override fun getCachedAppConfigVaccinationEventValidity(): Int {
        return getCachedAppConfig()?.vaccinationEventValidity
            ?: throw IllegalStateException("AppConfig should be cached")
    }

    override fun getCachedPublicKeys(): BufferedSource? {
        val publicKeysFile = File(filesDirPath, "public_keys.json")

        return appConfigStorageManager.getFileAsBufferedSource(publicKeysFile)
    }

    override fun getProviderName(providerIdentifier: String?): String {
        return getCachedAppConfig()?.providerIdentifiers?.firstOrNull { provider -> provider.code == providerIdentifier }?.name ?: ""
    }
}
