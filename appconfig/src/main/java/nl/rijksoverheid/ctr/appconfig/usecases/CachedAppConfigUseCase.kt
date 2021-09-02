package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import java.io.File

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CachedAppConfigUseCase {
    fun isCachedAppConfigValid(): Boolean
    fun getCachedAppConfig(): AppConfig
}

class CachedAppConfigUseCaseImpl constructor(
    private val appConfigStorageManager: AppConfigStorageManager,
    private val filesDirPath: String,
    private val moshi: Moshi,
    private val isVerifierApp: Boolean,
) : CachedAppConfigUseCase {
    private val configFile = File(filesDirPath, "config.json")

    private val defaultConfig = if (isVerifierApp) {
        VerifierConfig.default()
    } else {
        HolderConfig.default()
    }

    override fun isCachedAppConfigValid(): Boolean {
        return try {
            if (isVerifierApp) {
                appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()?.toObject<VerifierConfig>(moshi) is VerifierConfig
            } else {
                appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()?.toObject<HolderConfig>(moshi) is HolderConfig
            }
        } catch (exc: Exception) {
            false
        }
    }

    override fun getCachedAppConfig(): AppConfig {
        return try {
            val config = if (isVerifierApp) {
                appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()
                    ?.toObject(moshi) as? VerifierConfig
            } else {
                appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()
                    ?.toObject(moshi) as? HolderConfig
            }
            return config ?: defaultConfig
        } catch (exc: Exception) {
            defaultConfig
        }
    }
}
