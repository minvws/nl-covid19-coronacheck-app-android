package nl.rijksoverheid.ctr.appconfig.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun persist(appConfigContents: String, publicKeyContents: String): StorageResult
}

class PersistConfigUseCaseImpl(
    private val appConfigStorageManager: AppConfigStorageManager,
    private val filesDirPath: String,
) : PersistConfigUseCase {

    override suspend fun persist(appConfigContents: String, publicKeyContents: String) =
        withContext(Dispatchers.IO) {

            val publicKeysFile = File(filesDirPath, "public_keys.json")
            val publicKeysStorageResult = appConfigStorageManager.storageFile(publicKeysFile, publicKeyContents)
            if (publicKeysStorageResult is StorageResult.Error) {
                return@withContext publicKeysStorageResult
            }

            val configFile = File(filesDirPath, "config.json")
            val configStorageResult = appConfigStorageManager.storageFile(configFile, appConfigContents)
            if (configStorageResult is StorageResult.Error) {
                return@withContext configStorageResult
            }

            return@withContext StorageResult.Success
        }
}
