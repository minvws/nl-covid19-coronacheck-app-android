package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import java.io.File
import java.security.MessageDigest
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import org.json.JSONObject

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
    fun getCachedAppConfigOrNull(): AppConfig?
    fun getCachedAppConfigHash(): String
}

class CachedAppConfigUseCaseImpl constructor(
    private val appConfigStorageManager: AppConfigStorageManager,
    private val filesDirPath: String,
    private val moshi: Moshi,
    private val isVerifierApp: Boolean
) : CachedAppConfigUseCase {
    private val configFile = File(filesDirPath, "config.json")
    private val publicKeysFile = File(filesDirPath, "public_keys.json")

    private val defaultConfig = if (isVerifierApp) {
        VerifierConfig.default()
    } else {
        HolderConfig.default()
    }

    override fun isCachedAppConfigValid(): Boolean {
        return try {
            if (isVerifierApp) {
                appConfigStorageManager.getFileAsBufferedSource(configFile)?.toObject<VerifierConfig>(moshi) is VerifierConfig
                appConfigStorageManager.getFileAsBufferedSource(publicKeysFile)?.toObject<JSONObject>(moshi)?.has("eu_keys") == true
            } else {
                appConfigStorageManager.getFileAsBufferedSource(configFile)?.toObject<HolderConfig>(moshi) is HolderConfig
            }
        } catch (exc: Exception) {
            false
        }
    }

    override fun getCachedAppConfig(): AppConfig {
        return getCachedAppConfigOrNull() ?: defaultConfig
    }

    override fun getCachedAppConfigOrNull(): AppConfig? {
        if (!configFile.exists()) {
            return null
        }

        return try {
            val config = if (isVerifierApp) {
                appConfigStorageManager.getFileAsBufferedSource(configFile)
                    ?.toObject(moshi) as? VerifierConfig
            } else {
                appConfigStorageManager.getFileAsBufferedSource(configFile)
                    ?.toObject(moshi) as? HolderConfig
            }
            return config
        } catch (exc: Exception) {
            null
        }
    }

    override fun getCachedAppConfigHash(): String {
        val json = try {
            appConfigStorageManager.getFileAsBufferedSource(configFile)?.replace("\\/", "/") ?: return ""
        } catch (exc: Exception) {
            return ""
        }
        val bytes = json.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        // Return first 7 characters of hash
        return digest.fold("") { str, it -> str + "%02x".format(it) }.subSequence(0, 7).toString()
    }
}
