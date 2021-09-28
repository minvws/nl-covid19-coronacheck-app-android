package nl.rijksoverheid.ctr.holder

import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.json.JsonObjectJsonAdapter
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModelImpl
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.appconfig.isVerifierApp
import nl.rijksoverheid.ctr.appconfig.persistence.*
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepository
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepositoryImpl
import nl.rijksoverheid.ctr.appconfig.usecases.*
import okhttp3.CertificatePinner
import okhttp3.CertificatePinner.Companion.sha256Hash
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException
import kotlin.test.assertEquals


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
    fun `given a server with certificate and key, when ssl connect to it, then it returns 200`() = runBlocking {
        val server = mockServer()
        server.enqueue(MockResponse().apply {
            setBody("{}")
        })

        unloadKoinModules(appConfigModule(BuildConfig.CDN_API_URL, "holder", BuildConfig.VERSION_CODE))
        loadKoinModules(listOf(apiModule(server.url("/")), appConfigModule(server.url("/").toString(), "holder", BuildConfig.VERSION_CODE)))

        val configApi: AppConfigApi = get()

        val response = configApi.getConfig()

        assertEquals(200, response.code())
    }

    @Test
    fun `given a middle in the man server, when ssl connect to it, then the request fails`() {
        val server = MockWebServer().apply {
            val otherCertificate: HeldCertificate = HeldCertificate.Builder()
                .addSubjectAlternativeName(localhost)
                .build()
            val serverCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
                .heldCertificate(otherCertificate)
                .build()
            useHttps(serverCertificates.sslSocketFactory(), false)
        }
        server.enqueue(MockResponse().apply {
            setBody("{}")
        })

        loadKoinModules(apiModule(server.url("/")))

        val configApi: AppConfigApi = get()

        assertThrows(SSLHandshakeException::class.java) {
            runBlocking {
                configApi.getConfig()
            }
        }
    }

    private fun apiModule(baseUrl: HttpUrl) = module(override = true) {
        val certificateHash = "sha256/${localhostCertificate.certificate.sha256Hash().base64()}"
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
