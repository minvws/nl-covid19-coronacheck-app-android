/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepositoryImpl
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigUseCaseImpl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class AppConfigUseCaseImplTest {

    private val appConfig = AppConfig(
        minimumVersion = 0,
        appDeactivated = false,
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

    private val publicKeys = "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType())

    private val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
    private val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)

    @Test
    fun `config returns Success when both calls succeed`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig = appConfig
            override suspend fun getPublicKeys(): ResponseBody = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase =
            AppConfigUseCaseImpl(clock, appConfigPersistenceManager, configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Success(
                appConfig = appConfig,
                publicKeys = publicKeys.source()
            )
        )
        coVerify { appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(0) }
    }

    @Test
    fun `config returns Error when config call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig {
                throw IOException()
            }

            override suspend fun getPublicKeys(): ResponseBody = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase =
            AppConfigUseCaseImpl(clock, appConfigPersistenceManager, configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Error
        )
        coVerify(exactly = 0) { appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(0) }
    }

    @Test
    fun `config returns Error when public keys call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig = appConfig
            override suspend fun getPublicKeys(): ResponseBody {
                throw IOException()
            }
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase =
            AppConfigUseCaseImpl(clock, appConfigPersistenceManager, configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Error
        )
        coVerify(exactly = 0) { appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(0) }
    }
}
