/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.persistence

import java.io.File
import java.io.IOException
import okio.buffer
import okio.source
import okio.use

interface AppConfigStorageManager {
    fun storageFile(file: File, fileContents: String): StorageResult
    fun areConfigFilesPresentInFilesFolder(): Boolean
    fun getFileAsBufferedSource(file: File): String?
}

class AppConfigStorageManagerImpl(private val filesDirPath: String) : AppConfigStorageManager {
    override fun storageFile(file: File, fileContents: String): StorageResult {
        return try {
            file.parentFile?.mkdirs()
            file.bufferedWriter().use {
                it.write(fileContents)
            }
            StorageResult.Success
        } catch (exception: IOException) {
            StorageResult.Error(exception.message ?: "error storing: $file")
        }
    }

    override fun areConfigFilesPresentInFilesFolder(): Boolean {
        val configFileInFilesFolder = File(filesDirPath, "config.json")
        val publicKeysInFilesFolder = File(filesDirPath, "public_keys.json")

        return configFileInFilesFolder.exists() && publicKeysInFilesFolder.exists()
    }

    override fun getFileAsBufferedSource(file: File): String? {
        if (file.exists()) {
            return file.source().use {
                it.buffer().readUtf8()
            }
        }
        return null
    }
}

sealed class StorageResult {
    object Success : StorageResult()
    data class Error(val message: String) : StorageResult()
}
