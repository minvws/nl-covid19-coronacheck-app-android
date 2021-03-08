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
import nl.rijksoverheid.ctr.appconfig.model.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecase.AppConfigUseCaseImpl
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AppConfigUseCaseImplTest {

    private val appConfig = AppConfig(
        appDeactivated = true,
        minimumVersion = 0,
        informationURL = "dummy",
        configTtlSeconds = 0,
        maxValidityHours = 0
    )

    private val publicKeys = PublicKeys(
        clKeys = listOf()
    )

    @Test
    fun `config returns Success when both calls succeed`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig = appConfig
            override suspend fun getPublicKeys(): PublicKeys = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase = AppConfigUseCaseImpl(configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.Success(
                appConfig = appConfig,
                publicKeys = publicKeys
            )
        )
    }

    @Test
    fun `config returns NetworkError when config call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig {
                throw IOException()
            }

            override suspend fun getPublicKeys(): PublicKeys = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase = AppConfigUseCaseImpl(configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.NetworkError
        )
    }

    @Test
    fun `config returns ServerError when config call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig {
                throw HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                )
            }

            override suspend fun getPublicKeys(): PublicKeys = publicKeys
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase = AppConfigUseCaseImpl(configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.ServerError
        )
    }

    @Test
    fun `config returns NetworkError when public keys call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig = appConfig
            override suspend fun getPublicKeys(): PublicKeys {
                throw IOException()
            }
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase = AppConfigUseCaseImpl(configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.NetworkError
        )
    }

    @Test
    fun `config returns ServerError when public keys call fails`() = runBlocking {
        val fakeApi = object : AppConfigApi {
            override suspend fun getConfig(): AppConfig = appConfig
            override suspend fun getPublicKeys(): PublicKeys {
                throw HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                )
            }
        }
        val configRepository = ConfigRepositoryImpl(api = fakeApi)
        val appConfigUseCase = AppConfigUseCaseImpl(configRepository)
        assertEquals(
            appConfigUseCase.get(), ConfigResult.ServerError
        )
    }
}
