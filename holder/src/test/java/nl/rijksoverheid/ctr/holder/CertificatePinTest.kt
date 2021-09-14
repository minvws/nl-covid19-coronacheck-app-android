package nl.rijksoverheid.ctr.holder

import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.json.JsonObjectJsonAdapter
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import okhttp3.CertificatePinner
import okhttp3.CertificatePinner.Companion.sha256Hash
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okhttp3.tls.decodeCertificatePem
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.net.InetAddress
import org.koin.test.get
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class CertificatePinTest: AutoCloseKoinTest() {

    private val localhost = InetAddress.getByName("localhost").canonicalHostName
    private val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName(localhost)
        .build()
    private val serverCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
        .heldCertificate(localhostCertificate)
        .build()

    private fun mockServer() = MockWebServer().apply {
        useHttps(serverCertificates.sslSocketFactory(), false)
    }

    @Test
    fun test() = runBlocking {
        val server = mockServer()
        server.enqueue(MockResponse().apply {
            setBody("{}")
        })

        loadKoinModules(apiModule(server.url("/")))

        val configApi: AppConfigApi = get()

        val response = configApi.getConfig()
        println(response.raw().request.url)
    }

    private fun apiModule(baseUrl: HttpUrl) = module(override = true) {
        val certificateHash = "sha256/${localhostCertificate.certificate.sha256Hash().base64()}"
        println("GIO $certificateHash")
        single {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(false)
                .certificatePinner(
                    CertificatePinner.Builder()
                        .add(baseUrl.host, certificateHash).build()
                )
                .apply {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .addTrustedCertificate(localhostCertificate.certificate)
                        .build()
                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
                .build()
        }

        single {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }

        single {
            Moshi.Builder()
                .add(JsonObjectJsonAdapter())
        }
    }
}