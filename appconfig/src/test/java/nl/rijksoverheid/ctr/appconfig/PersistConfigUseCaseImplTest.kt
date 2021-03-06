package nl.rijksoverheid.ctr.appconfig

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.usecase.PersistConfigUseCaseImpl
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

    @Test
    fun `Usecase persists configs locally`() = runBlocking {
        val appConfig = AppConfig(
            appDeactivated = true,
            minimumVersion = 0,
            informationURL = "dummy",
            configTtlSeconds = 0,
            maxValidityHours = 0
        )

        val publicKeys = PublicKeys(
            clKeys = listOf()
        )

        val usecase = PersistConfigUseCaseImpl(
            appConfigPersistenceManager = appConfigPersistenceManager
        )

        usecase.persist(
            appConfig = appConfig,
            publicKeys = publicKeys
        )

        coVerify { appConfigPersistenceManager.saveAppConfigJson(any()) }
        coVerify { appConfigPersistenceManager.savePublicKeysJson(any()) }
    }

}
