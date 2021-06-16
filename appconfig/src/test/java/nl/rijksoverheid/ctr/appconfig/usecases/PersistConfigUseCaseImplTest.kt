package nl.rijksoverheid.ctr.appconfig.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import okio.BufferedSource
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PersistConfigUseCaseImplTest {

    private val appConfigPersistenceManager: AppConfigPersistenceManager = mockk(relaxed = true)
    private val appConfigStorageManager: AppConfigStorageManager = mockk(relaxed = true)

    @Test
    fun `Usecase persists configs locally`() = runBlocking {
        val appConfig = "{}"

        val publicKeys = mockk<BufferedSource>()
        coEvery { publicKeys.readUtf8() } returns "file contents"

        val usecase = PersistConfigUseCaseImpl(
            appConfigPersistenceManager = appConfigPersistenceManager,
            appConfigStorageManager = appConfigStorageManager,
            cacheDir = "",
            isVerifierApp = false,
        )

        usecase.persist(
            appConfigContents = appConfig,
            publicKeyContents = publicKeys.readUtf8()
        )

        coVerify { appConfigPersistenceManager.saveAppConfigJson(any()) }
        coVerify { appConfigStorageManager.storageFile(any(), any()) }
    }

}
