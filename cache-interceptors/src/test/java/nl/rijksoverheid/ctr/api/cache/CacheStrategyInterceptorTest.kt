/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.api.cache

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.cachestrategy.CacheStrategy
import nl.rijksoverheid.ctr.api.cachestrategy.CacheStrategyInterceptor
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
import retrofit2.http.Tag
import java.io.File

private const val RESPONSE = "Server response"

class CacheStrategyInterceptorTest {
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
            OkHttpClient.Builder().cache(cache).addInterceptor(CacheStrategyInterceptor()).build()
        ).baseUrl(mockWebServer.url("/")).build().create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `cacheStrategy CACHE_FIRST will try the cache first, then network`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(RESPONSE))
        testApi.dummyRequest(CacheStrategy.CACHE_FIRST)
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun `cacheStrategy CACHE_FIRST returns cached response`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(RESPONSE))
        // network request
        testApi.dummyRequest(CacheStrategy.CACHE_LAST)
        testApi.dummyRequest(CacheStrategy.CACHE_FIRST)
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun `cacheStrategy CACHE_LAST will try the cache in case of an error response`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(RESPONSE))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        val response = testApi.dummyRequest()
        val cachedResponse = testApi.dummyRequest(CacheStrategy.CACHE_LAST)

        assertEquals(200, response.code())
        assertEquals(200, cachedResponse.code())
        assertEquals(RESPONSE, cachedResponse.body()?.string())
        assertEquals(2, mockWebServer.requestCount)
    }

    @Test
    fun `cacheStrategy CACHE_ONLY will try the cache the cache only`() = runBlocking {
        val response = testApi.dummyRequest(CacheStrategy.CACHE_ONLY)
        assertEquals(504, response.code())
        assertEquals(0, mockWebServer.requestCount)
    }

    interface TestApi {
        @GET("/")
        suspend fun dummyRequest(@Tag cacheStrategy: CacheStrategy = CacheStrategy.CACHE_LAST): Response<ResponseBody>
    }
}