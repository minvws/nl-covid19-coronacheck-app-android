/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.api.FakeTestApiClient
import nl.rijksoverheid.ctr.shared.models.AppStatus
import nl.rijksoverheid.ctr.shared.models.Config
import nl.rijksoverheid.ctr.shared.models.ConfigType
import nl.rijksoverheid.ctr.shared.repositories.ConfigRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStatusUseCaseTest {

    @Test
    fun `status returns AppDeactivated when app is deactivated remotely`() = runBlocking {
        val fakeApi = object : FakeTestApiClient() {
            override suspend fun getHolderConfig(): Config {
                return Config(
                    minimumVersion = 2,
                    message = "",
                    playStoreURL = "",
                    appDeactivated = true,
                    informationURL = ""
                )
            }
        }
        val configRepository = ConfigRepository(api = fakeApi)
        val appStatusUseCase = AppStatusUseCase(configRepository)
        val appStatus = appStatusUseCase.status(currentVersionCode = 1, type = ConfigType.Holder)
        assertEquals(appStatus, AppStatus.AppDeactivated)
    }

    @Test
    fun `status returns ShouldUpdate when remote version code is higher than current`() =
        runBlocking {
            val fakeApi = object : FakeTestApiClient() {
                override suspend fun getHolderConfig(): Config {
                    return Config(
                        minimumVersion = 2,
                        message = "",
                        playStoreURL = "",
                        appDeactivated = false,
                        informationURL = ""
                    )
                }
            }
            val configRepository = ConfigRepository(api = fakeApi)
            val appStatusUseCase = AppStatusUseCase(configRepository)
            val appStatus =
                appStatusUseCase.status(currentVersionCode = 1, type = ConfigType.Holder)
            assertEquals(appStatus, AppStatus.ShouldUpdate)
        }

    @Test
    fun `status returns Ok when allowed`() = runBlocking {
        val fakeApi = object : FakeTestApiClient() {
            override suspend fun getHolderConfig(): Config {
                return Config(
                    minimumVersion = 1,
                    message = "",
                    playStoreURL = "",
                    appDeactivated = false,
                    informationURL = ""
                )
            }
        }
        val configRepository = ConfigRepository(api = fakeApi)
        val appStatusUseCase = AppStatusUseCase(configRepository)
        val appStatus =
            appStatusUseCase.status(currentVersionCode = 1, type = ConfigType.Holder)
        assertEquals(appStatus, AppStatus.Ok)
    }
}
