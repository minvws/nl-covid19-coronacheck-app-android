/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.appconfig

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApiCacheInterceptor
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.File

private const val RESPONSE = "Server response"

class AppConfigApiCacheInterceptorTest {

    private val fakeCachedAppConfigUseCase = object : CachedAppConfigUseCase {
        override fun persistAppConfig(appConfig: AppConfig) {

        }

        override fun getCachedAppConfig(): AppConfig {
            return AppConfig(
                minimumVersion = 0,
                appDeactivated = false,
                informationURL = "",
                configTtlSeconds = 30,
                maxValidityHours = 0
            )
        }

        override fun persistPublicKeys(publicKeys: PublicKeys) {}

        override fun getCachedPublicKeys(): PublicKeys {
            return PublicKeys(clKeys = listOf())
        }

    }

    private lateinit var mockWebServer: MockWebServer
    private lateinit var cache: Cache
    private lateinit var tmpDir: File
    private lateinit var testApi: TestApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        tmpDir = File.createTempFile("cache", "dir")
        tmpDir.delete()
        cache = Cache(tmpDir, 1024 * 1024)
        testApi = Retrofit.Builder().client(
            OkHttpClient.Builder().cache(cache)
                .addInterceptor(AppConfigApiCacheInterceptor(fakeCachedAppConfigUseCase))
                .build()
        ).baseUrl(mockWebServer.url("/")).build().create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `try the cache in case of an error response`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(RESPONSE))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        val response = testApi.dummyRequest()
        val cachedResponse = testApi.dummyRequest()

        assertEquals(200, response.code())
        assertEquals(200, cachedResponse.code())
        assertEquals(RESPONSE, cachedResponse.body()?.string())
        assertEquals(2, mockWebServer.requestCount)
    }

    interface TestApi {
        @GET("/")
        suspend fun dummyRequest(): Response<ResponseBody>
    }
}
