/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.appconfig.usecase.AppConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AppStatusUseCaseTest {

    @Test
    fun `status returns Deactivated when app is deactivated remotely`() = runBlocking {
        val informationUrl = "https://website.nl"
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                AppConfig(
                    appDeactivated = true,
                    minimumVersion = 2,
                    informationURL = informationUrl,
                    message = "deactivated"
                )

            override suspend fun getPublicKeys(): PublicKeys =
                PublicKeys(clKeys = listOf())
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository)
        val appStatus = appStatusUseCase.status(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.Deactivated(informationUrl))
    }

    @Test
    fun `status returns UpdateRequired when remote version code is higher than current`() =
        runBlocking {
            val fakeApi = object : AppConfigApi {
                override suspend fun getConfig(): AppConfig =
                    AppConfig(
                        minimumVersion = 2,
                        informationURL = "http://www.website.nl"
                    )

                override suspend fun getPublicKeys(): PublicKeys =
                    PublicKeys(clKeys = listOf())
            }

            val configRepository = ConfigRepositoryImpl(api = fakeApi)
            val appStatusUseCase = AppConfigUseCase(configRepository)
            val appStatus =
                appStatusUseCase.status(currentVersionCode = 1)
            assertEquals(appStatus, AppStatus.UpdateRequired)
        }

    @Test
    fun `status returns UpdateRequired with a message when remote version code is higher than current`() =
        runBlocking {
            val fakeApi = object : AppConfigApi {
                override suspend fun getConfig(): AppConfig =
                    AppConfig(
                        minimumVersion = 2,
                        informationURL = "http://www.website.nl"
                    )

                override suspend fun getPublicKeys(): PublicKeys =
                    PublicKeys(clKeys = listOf())
            }
            val configRepository = ConfigRepositoryImpl(api = fakeApi)
            val appStatusUseCase = AppConfigUseCase(configRepository)
            val appStatus =
                appStatusUseCase.status(currentVersionCode = 1)
            assertEquals(appStatus, AppStatus.UpdateRequired)
        }

    @Test
    fun `status returns NoActionRequired when app is up to date`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                AppConfig(
                    minimumVersion = 2,
                    informationURL = "http://www.website.nl"
                )

            override suspend fun getPublicKeys(): PublicKeys =
                PublicKeys(clKeys = listOf())
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository)
        val appStatus = appStatusUseCase.status(currentVersionCode = 2)
        assertEquals(appStatus, AppStatus.NoActionRequired)
    }

    @Test
    fun `status returns InternetRequired when config request fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                throw IOException()

            override suspend fun getPublicKeys(): PublicKeys =
                PublicKeys(clKeys = listOf())
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository)
        val appStatus = appStatusUseCase.status(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.InternetRequired)
    }

    @Test
    fun `status returns InternetRequired when public keys request fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig =
                AppConfig(
                    minimumVersion = 2,
                    informationURL = "http://www.website.nl"
                )

            override suspend fun getPublicKeys(): PublicKeys =
                throw IOException()
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appStatusUseCase = AppConfigUseCase(configRepository)
        val appStatus = appStatusUseCase.status(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.InternetRequired)
    }
}
