package nl.rijksoverheid.ctr.api

import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Retrofit
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.TrustManagerFactory

import okhttp3.tls.HandshakeCertificates

import okhttp3.mockwebserver.MockResponse

import okhttp3.mockwebserver.MockWebServer

import okhttp3.tls.HeldCertificate
import org.junit.runner.RunWith

import java.net.InetAddress
import java.security.interfaces.RSAPrivateCrtKey


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CertificatePinningTest {

    private val mockWebServer = MockWebServer()
    private lateinit var testApi: TestApi

    private fun sslContext(): SSLContext {
        // keystore.jks is a keystore containing a certificate and a public key, generated with
        // https://github.com/minvws/nl-covid19-coronacheck-provider-docs/blob/main/signing-demo/shellscript/gen-fake-pki-overheid.sh
        val stream = this.javaClass.classLoader?.getResourceAsStream("keystore.jks")
        val serverKeyStorePassword = "3ekleidwseme".toCharArray()
        val serverKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        serverKeyStore.load(stream, serverKeyStorePassword)

        val kmfAlgorithm = KeyManagerFactory.getDefaultAlgorithm()
        val kmf = KeyManagerFactory.getInstance(kmfAlgorithm)
        kmf.init(serverKeyStore, serverKeyStorePassword)

        val trustManagerFactory = TrustManagerFactory.getInstance(kmfAlgorithm)
        trustManagerFactory.init(serverKeyStore)

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(kmf.keyManagers, trustManagerFactory.trustManagers, null)
        return sslContext
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun mockServer() = MockWebServer().apply {
        val localhost = InetAddress.getByName("localhost").canonicalHostName
        val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
            .addSubjectAlternativeName(localhost)
            .build()
        val serverCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()
        useHttps(serverCertificates.sslSocketFactory(), false)
    }

    @Test
    fun mock() {
        val server = mockServer()
        server.enqueue(MockResponse())


    }

    @Test
    fun `given an invalid certificate, when calling an endpoint, then it throws SSLPeerUnverifiedException`() {
        testApi = Retrofit.Builder().client(
            OkHttpClient.Builder()
                .certificatePinner(
                    CertificatePinner.Builder().add(
                        "test.com", "sha256/AAAAAAAA"
                    ).build()
                )
                .build()
        ).baseUrl(mockWebServer.url("https://test.com")).build()
            .create(TestApi::class.java)
        mockWebServer.useHttps(sslContext().socketFactory, false)

        mockWebServer.enqueue(
            MockResponse()
                .setBody("Server response")
        )

        val exception = assertThrows(
            SSLPeerUnverifiedException::class.java
        ) {
            runBlocking {
                testApi.normalRequest()
            }
        }
        assertEquals(
            "Certificate pinning failure!\n  Peer certificate chain:\n" +
                    "    sha256/C60BnPKITwdkeocPJsnaBG0oy37zbH9X5VbaPILmq1s=: CN=www.test.com\n" +
                    "    sha256/G8g19iXohPy9KZGriQ807A59lcylMu81Zi8hXz5xxsM=: CN=Network Solutions DV Server CA 2, O=Network Solutions L.L.C., L=Herndon, ST=VA, C=US\n" +
                    "    sha256/x4QzPSC810K5/cMjb05Qm4k3Bw5zBn4lTdO/nEW/Td4=: CN=USERTrust RSA Certification Authority, O=The USERTRUST Network, L=Jersey City, ST=New Jersey, C=US\n" +
                    "  Pinned certificates for test.com:\n" +
                    "    sha256/AAAAAAAA", exception.message
        )
    }

    @Test
    fun `given valid certificates, when calling an endpoint, then network request works`() =
        runBlocking {
            testApi = Retrofit.Builder().client(
                OkHttpClient.Builder()
                    .certificatePinner(
                        CertificatePinner.Builder().add(
                            "test.com",
                            "sha256/C60BnPKITwdkeocPJsnaBG0oy37zbH9X5VbaPILmq1s=",
                            "sha256/G8g19iXohPy9KZGriQ807A59lcylMu81Zi8hXz5xxsM=",
                            "sha256/x4QzPSC810K5/cMjb05Qm4k3Bw5zBn4lTdO/nEW/Td4="
                        ).build()
                    )
                    .build()
            ).baseUrl(mockWebServer.url("https://test.com")).build()
                .create(TestApi::class.java)
            mockWebServer.useHttps(sslContext().socketFactory, false)

            mockWebServer.enqueue(
                MockResponse()
                    .setBody("Server response")
            )

            val response = testApi.normalRequest()

            assertEquals("OK", response.message())
        }
}
