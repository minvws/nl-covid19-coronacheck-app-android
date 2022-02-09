package nl.rijksoverheid.ctr.holder.persistence

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.shared.DebugDisclosurePolicyPersistenceManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import java.io.File

interface CachedAppConfigUseCase {
    fun getCachedAppConfig(): HolderConfig
}

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CachedAppConfigUseCaseImpl constructor(
    private val appConfigStorageManager: AppConfigStorageManager,
    private val filesDirPath: String,
    private val moshi: Moshi,
    private val isDebugApp: Boolean,
    private val debugDisclosurePolicyPersistenceManager: DebugDisclosurePolicyPersistenceManager
) : CachedAppConfigUseCase {

    private val configFile = File(filesDirPath, "config.json")
    private val defaultConfig = HolderConfig.default()

    override fun getCachedAppConfig(): HolderConfig {
        if (!configFile.exists()) {
            return defaultConfig
        }

        return try {
            val config = appConfigStorageManager.getFileAsBufferedSource(configFile)?.readUtf8()
                ?.toObject(moshi)
                ?: defaultConfig
            val debugPolicy = debugDisclosurePolicyPersistenceManager.getDebugDisclosurePolicy()

            if (isDebugApp && debugPolicy != null) {
                config.copy(disclosurePolicy = debugPolicy)
            } else {
                config
            }
        } catch (exc: Exception) {
            defaultConfig
        }
    }
}
