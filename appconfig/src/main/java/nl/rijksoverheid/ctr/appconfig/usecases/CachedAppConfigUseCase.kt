package nl.rijksoverheid.ctr.appconfig

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
    fun getCachedAppConfigMaxValidityHours(): Int
    fun getCachedAppConfigVaccinationEventValidity(): Int
    fun getCachedPublicKeys(): BufferedSource?
    fun getProviderName(providerIdentifier: String?): String
}

class CachedAppConfigUseCaseImpl constructor(
    private val appConfigStorageManager: AppConfigStorageManager,
    private val cacheDir: String,
    private val moshi: Moshi
) : CachedAppConfigUseCase {

    override fun getCachedAppConfig(): AppConfig? {
        val configFile = File(cacheDir, "config.json")
        return appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()?.toObject(moshi)
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
        val publicKeysFile = File(cacheDir, "public_keys.json")
        return appConfigStorageManager.getFileAsBufferedSource(publicKeysFile)
    }

    override fun getProviderName(providerIdentifier: String?): String {
        return getCachedAppConfig()?.providerIdentifiers?.firstOrNull { provider -> provider.code == providerIdentifier }?.name ?: ""
    }
}
