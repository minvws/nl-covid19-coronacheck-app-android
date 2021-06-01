package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.persistence.StorageResult
import java.io.File

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface PersistConfigUseCase {
    suspend fun persist(appConfig: AppConfig, publicKeys: PublicKeys): StorageResult
}

class PersistConfigUseCaseImpl(
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val cacheDir: String,
    private val moshi: Moshi
) : PersistConfigUseCase {

    override suspend fun persist(appConfig: AppConfig, publicKeys: PublicKeys) =
        withContext(Dispatchers.IO) {
            val configContents = appConfig.toJson(moshi)
            appConfigPersistenceManager.saveAppConfigJson(
                json = configContents
            )
            val publicKeysContents = publicKeys.toJson(moshi)
            appConfigPersistenceManager.savePublicKeysJson(
                json = publicKeysContents
            )

            val configFile = File(cacheDir, "config.json")
            val publicKeysFile = File(cacheDir, "public_keys.json")
            val configStorageResult = appConfigStorageManager.storageFile(configFile, configContents)
            val publicKeysStorageResult = appConfigStorageManager.storageFile(publicKeysFile, publicKeysContents)


            return@withContext if (configStorageResult is StorageResult.Success && publicKeysStorageResult is StorageResult.Success) {
                StorageResult.Success
            } else if (configStorageResult is StorageResult.Error) {
                configStorageResult
            } else {
                publicKeysStorageResult
            }
        }
}
