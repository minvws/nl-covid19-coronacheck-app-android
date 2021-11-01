/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepositoryImpl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class AppConfigUseCaseImplTest {

    private val appConfig = Response.success(JSONObject())

    private val publicKeys = "".toResponseBody("application/json".toMediaType())

    private val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
    private val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)
    private val clockDeviationUseCase = mockk<ClockDeviationUseCaseImpl>(relaxed = true)

    private val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>().apply {
        every { getCachedAppConfig() } returns object :
            AppConfig(true, "", 1, 3600, 100, emptyList(), 1, 1, listOf(), 30, 300) {}
    }

    @Test
    fun `config returns Success when both calls succeed`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): Response<JSONObject> = appConfig
            override suspend fun getPublicKeys(): ResponseBody = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase =
            AppConfigUseCaseImpl(
                clock,
                appConfigPersistenceManager,
                configRepository,
                clockDeviationUseCase
            )
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Success(
                appConfig = appConfig.body().toString(),
                publicKeys = publicKeys.source().readUtf8()
            )
        )
        coVerify { appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(0) }
    }

    @Test
    fun `config returns Error when config call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): Response<JSONObject> {
                throw IOException()
            }

            override suspend fun getPublicKeys(): ResponseBody = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase =
            AppConfigUseCaseImpl(
                clock,
                appConfigPersistenceManager,
                configRepository,
                clockDeviationUseCase
            )
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Error
        )
        coVerify(exactly = 0) { appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(0) }
    }

    @Test
    fun `config returns Error when public keys call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): Response<JSONObject> = appConfig
            override suspend fun getPublicKeys(): ResponseBody {
                throw IOException()
            }
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase =
            AppConfigUseCaseImpl(
                clock,
                appConfigPersistenceManager,
                configRepository,
                clockDeviationUseCase
            )
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Error
        )
        coVerify(exactly = 0) { appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(0) }
    }

    @Test
    fun `given config refreshed 60 seconds ago with 100 seconds minimum interval, when try to refresh it, then it does not refresh`() {
        val now = Instant.parse("2021-10-20T12:00:00.00Z")
        val appConfigLastFetchedSeconds = now.minusSeconds(60).epochSecond
        val clock = Clock.fixed(now, ZoneId.of("UTC"))
        every { appConfigPersistenceManager.getAppConfigLastFetchedSeconds() } returns appConfigLastFetchedSeconds
        val appConfigUseCase =
            AppConfigUseCaseImpl(
                clock,
                appConfigPersistenceManager,
                mockk(),
                clockDeviationUseCase
            )

        val canRefresh = appConfigUseCase.canRefresh(cachedAppConfigUseCase)

        assertFalse(canRefresh)
    }

    @Test
    fun `given config refreshed 600 seconds ago with 100 seconds minimum interval, when try to refresh it, then it does not refresh`() {
        val now = Instant.parse("2021-10-20T12:00:00.00Z")
        val appConfigLastFetchedSeconds = now.minusSeconds(600).epochSecond
        val clock = Clock.fixed(now, ZoneId.of("UTC"))
        every { appConfigPersistenceManager.getAppConfigLastFetchedSeconds() } returns appConfigLastFetchedSeconds
        val appConfigUseCase =
            AppConfigUseCaseImpl(
                clock,
                appConfigPersistenceManager,
                mockk(),
                clockDeviationUseCase
            )

        val canRefresh = appConfigUseCase.canRefresh(cachedAppConfigUseCase)

        assertTrue(canRefresh)
    }
}
