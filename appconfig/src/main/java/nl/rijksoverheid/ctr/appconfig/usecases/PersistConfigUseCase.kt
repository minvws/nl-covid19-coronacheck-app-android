package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.persistence.StorageResult
import okhttp3.ResponseBody
import okio.BufferedSource
import java.io.File
import java.io.InputStream

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface PersistConfigUseCase {
    suspend fun persist(appConfig: AppConfig, publicKeys: BufferedSource): StorageResult
}

class PersistConfigUseCaseImpl(
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val isVerifierApp: Boolean,
    private val cacheDir: String,
    private val moshi: Moshi
) : PersistConfigUseCase {

    override suspend fun persist(appConfig: AppConfig, publicKeys: BufferedSource) =
        withContext(Dispatchers.IO) {
            val configContents = appConfig.toJson(moshi)
            appConfigPersistenceManager.saveAppConfigJson(
                json = configContents
            )
//            val publicKeysContents = publicKeys.toJson(moshi)
//            appConfigPersistenceManager.savePublicKeysJson(
//                json = publicKeysContents
//            )

            val publicKeysFile = File(cacheDir, "public_keys.json")
            val contents = publicKeys.readUtf8()
            val publicKeysStorageResult = appConfigStorageManager.storageFile(publicKeysFile, contents)
            if (publicKeysStorageResult is StorageResult.Error) {
                return@withContext publicKeysStorageResult
            }

            if (isVerifierApp) {
                val configFile = File(cacheDir, "config.json")
                val configStorageResult = appConfigStorageManager.storageFile(configFile, configContents)
                if (configStorageResult is StorageResult.Error) {
                    return@withContext configStorageResult
                }
            }

            return@withContext StorageResult.Success
        }
}
