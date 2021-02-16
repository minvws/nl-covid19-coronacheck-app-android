/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.cachestrategy.CacheStrategy
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStatusUseCaseTest {

    @Test
    fun `status returns Deactivated when app is deactivated remotely`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(cacheStrategy: CacheStrategy): AppConfig =
                AppConfig(appDeactivated = true, minimumVersion = 2)
        }
        val configRepository = ConfigRepository(api = fakeApi)
        val appStatusUseCase = AppStatusUseCase(configRepository)
        val appStatus = appStatusUseCase.status(
            currentVersionCode = 1
        )
        assertEquals(appStatus, AppStatus.Deactivated)
    }

    @Test
    fun `status returns UpdateRequired when remote version code is higher than current`() =
        runBlocking {
            val fakeApi = object : AppConfigApi {
                override suspend fun getConfig(cacheStrategy: CacheStrategy): AppConfig =
                    AppConfig(minimumVersion = 2)
            }
            val configRepository = ConfigRepository(api = fakeApi)
            val appStatusUseCase = AppStatusUseCase(configRepository)
            val appStatus =
                appStatusUseCase.status(currentVersionCode = 1)
            assertEquals(appStatus, AppStatus.UpdateRequired)
        }

    @Test
    fun `status returns UpToDate when app is up to date`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(cacheStrategy: CacheStrategy): AppConfig =
                AppConfig(minimumVersion = 2)
        }
        val configRepository = ConfigRepository(api = fakeApi)
        val appStatusUseCase = AppStatusUseCase(configRepository)
        val appStatus = appStatusUseCase.status(currentVersionCode = 2)
        assertEquals(appStatus, AppStatus.UpToDate)
    }
}