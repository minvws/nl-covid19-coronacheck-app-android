/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.appconfig.usecase.AppConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AppConfigUseCaseTest {

    private val fakeCachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)

    @Test
    fun `config returns Deactivated when app is deactivated remotely`() = runBlocking {
        val informationUrl = "https://website.nl"
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                AppConfig(
                    appDeactivated = true,
                    minimumVersion = 2,
                    informationURL = informationUrl,
                    configTtlSeconds = 0,
                    maxValidityHours = 0
                )

            override suspend fun getPublicKeys(): PublicKeys =
                PublicKeys(clKeys = listOf())
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository, fakeCachedAppConfigUseCase)
        val appStatus = appStatusUseCase.config(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.Deactivated(informationUrl))
    }

    @Test
    fun `config returns UpdateRequired when remote version code is higher than current`() =
        runBlocking {
            val fakeApi = object : AppConfigApi {
                override suspend fun getConfig(): AppConfig =
                    AppConfig(
                        appDeactivated = false,
                        minimumVersion = 2,
                        informationURL = "https://website.nl",
                        configTtlSeconds = 0,
                        maxValidityHours = 0
                    )

                override suspend fun getPublicKeys(): PublicKeys =
                    PublicKeys(clKeys = listOf())
            }

            val configRepository = ConfigRepositoryImpl(api = fakeApi)
            val appStatusUseCase = AppConfigUseCase(configRepository, fakeCachedAppConfigUseCase)
            val appStatus =
                appStatusUseCase.config(currentVersionCode = 1)
            assertEquals(appStatus, AppStatus.UpdateRequired)
        }


    @Test
    fun `config returns NoActionRequired when app is up to date`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                AppConfig(
                    appDeactivated = false,
                    minimumVersion = 2,
                    informationURL = "https://website.nl",
                    configTtlSeconds = 0,
                    maxValidityHours = 0
                )

            override suspend fun getPublicKeys(): PublicKeys =
                PublicKeys(clKeys = listOf())
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository, fakeCachedAppConfigUseCase)
        val appStatus = appStatusUseCase.config(currentVersionCode = 2)
        assertEquals(appStatus, AppStatus.NoActionRequired)
    }

    @Test
    fun `config returns InternetRequired when config request fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                throw IOException()

            override suspend fun getPublicKeys(): PublicKeys =
                PublicKeys(clKeys = listOf())
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository, fakeCachedAppConfigUseCase)
        val appStatus = appStatusUseCase.config(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.InternetRequired)
    }

    @Test
    fun `config returns InternetRequired when public keys request fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                AppConfig(
                    appDeactivated = true,
                    minimumVersion = 2,
                    informationURL = "https://website.nl",
                    configTtlSeconds = 0,
                    maxValidityHours = 0
                )

            override suspend fun getPublicKeys(): PublicKeys =
                throw IOException()
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository, fakeCachedAppConfigUseCase)
        val appStatus = appStatusUseCase.config(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.InternetRequired)
    }

    @Test
    fun `config persists config and public keys`() = runBlocking {
        val appConfig = AppConfig(
            appDeactivated = true,
            minimumVersion = 2,
            informationURL = "https://website.nl",
            configTtlSeconds = 0,
            maxValidityHours = 0
        )
        val publicKeys = PublicKeys(clKeys = listOf())
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig = appConfig
            override suspend fun getPublicKeys(): PublicKeys =
                publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)

        AppConfigUseCase(configRepository, fakeCachedAppConfigUseCase).config(
            currentVersionCode = 1
        )

        coVerify { fakeCachedAppConfigUseCase.persistAppConfig(appConfig) }
        coVerify { fakeCachedAppConfigUseCase.persistPublicKeys(publicKeys) }
    }

}
