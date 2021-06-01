/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.eu.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import okhttp3.ResponseBody
import java.io.*


interface PersistEuPublicKeysUsecase {
    suspend fun persist(fileName: String, euPublicKeys: ResponseBody): StoreFileResult
}

class PersistEuPublicKeysUsecaseImpl(private val cacheDir: File,
                                     private val persistenceManager: PersistenceManager) : PersistEuPublicKeysUsecase {
    override suspend fun persist(fileName: String, euPublicKeys: ResponseBody) = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val destinationFile = File(cacheDir, fileName)

        return@withContext try {
            inputStream = euPublicKeys.byteStream()
            outputStream = FileOutputStream(destinationFile)
            val data = ByteArray(4096)
            var count: Int = 0
            while (inputStream.read(data).also { count = it } != -1) {
                outputStream.write(data, 0, count)
            }
            outputStream.flush()
            persistenceManager.saveEuPublicKeyPath(cacheDir.path)
            println("GIO Succcess ${cacheDir.path}")
            StoreFileResult.Success
        } catch (e: IOException) {
            e.printStackTrace()
            StoreFileResult.Error(e.message ?: "error storing $fileName")
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

}

sealed class StoreFileResult {
    object Success: StoreFileResult()
    class Error(message: String): StoreFileResult()
}
