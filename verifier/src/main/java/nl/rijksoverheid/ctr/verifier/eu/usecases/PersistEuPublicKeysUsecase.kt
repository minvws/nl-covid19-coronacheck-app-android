/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.eu.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import okhttp3.ResponseBody
import java.io.*


interface PersistEuPublicKeysUsecase {
    suspend fun persist(euPublicKeys: ResponseBody)
}

class PersistEuPublicKeysUsecaseImpl(private val cacheDir: File, private val persistenceManager: PersistenceManager) : PersistEuPublicKeysUsecase {
    override suspend fun persist(euPublicKeys: ResponseBody) = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val destinationFile = File(cacheDir, "pubkey_filename")

        try {
            inputStream = euPublicKeys.byteStream()
            outputStream = FileOutputStream(destinationFile)
            val data = ByteArray(4096)
            var count: Int = 0
            while (inputStream.read(data).also { count = it } != -1) {
                outputStream.write(data, 0, count)
            }
            outputStream.flush()
            persistenceManager.saveEuPublicKeyPath(cacheDir.path)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }


}