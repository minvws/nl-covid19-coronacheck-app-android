package nl.rijksoverheid.ctr.appconfig.usecases

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.persistence.StorageResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface DeleteConfigUseCase {
    suspend operator fun invoke()
}

class DeleteConfigUseCaseImpl(
    private val filesDirPath: String
) : DeleteConfigUseCase {

    override suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            val configFile = File(filesDirPath, "config.json")
            val publicKeysFile = File(filesDirPath, "public_keys.json")

            configFile.delete()
            publicKeysFile.delete()
        }
    }
}
