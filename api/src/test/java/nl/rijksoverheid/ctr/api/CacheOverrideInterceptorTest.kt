/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.api

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.interceptors.CacheOverrideInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.io.File

private const val RESPONSE = "Server response"

class CacheOverrideInterceptorTest {
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
            OkHttpClient.Builder().addInterceptor(CacheOverrideInterceptor()).build()
        ).baseUrl(mockWebServer.url("/")).build().create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `CacheOverride overrides cache headers from server`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(RESPONSE)
                .addHeader("cache-control", "no-cache")
                .addHeader("pragma", "no-cache")
        )
        mockWebServer.enqueue(
            MockResponse()
                .setBody(RESPONSE)
                .addHeader("cache-control", "no-cache")
                .addHeader("pragma", "no-cache")
        )

        val normalResponse = testApi.normalRequest()
        val cacheOverrideResponse = testApi.cacheOverridden()

        assertEquals("no-cache", normalResponse.headers()["cache-control"])
        assertEquals("no-cache", normalResponse.headers()["pragma"])
        assertEquals("public,max-age=0", cacheOverrideResponse.headers()["cache-control"])
        assertNull(cacheOverrideResponse.headers()["pragma"])
    }

}

