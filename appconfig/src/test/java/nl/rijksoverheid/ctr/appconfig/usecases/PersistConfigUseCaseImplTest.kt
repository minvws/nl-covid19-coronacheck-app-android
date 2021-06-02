package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
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
        val appConfig = AppConfig(
            minimumVersion = 0,
            appDeactivated = true,
            informationURL = "dummy",
            configTtlSeconds = 0,
            maxValidityHours = 0,
            euLaunchDate = "",
            credentialRenewalDays = 0,
            domesticCredentialValidity = 0,
            testEventValidity = 0,
            recoveryEventValidity = 0,
            temporarilyDisabled = false,
            requireUpdateBefore = 0
        )

        val publicKeys = PublicKeys(
            clKeys = listOf()
        )

        val usecase = PersistConfigUseCaseImpl(
            appConfigPersistenceManager = appConfigPersistenceManager,
            appConfigStorageManager = appConfigStorageManager,
            cacheDir = "",
            moshi = Moshi.Builder().build()
        )

        usecase.persist(
            appConfig = appConfig,
            publicKeys = publicKeys
        )

        coVerify { appConfigPersistenceManager.saveAppConfigJson(any()) }
        coVerify { appConfigPersistenceManager.savePublicKeysJson(any()) }
    }

}
