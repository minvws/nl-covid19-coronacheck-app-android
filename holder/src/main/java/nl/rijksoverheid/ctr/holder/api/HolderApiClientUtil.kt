package nl.rijksoverheid.ctr.holder.api

import android.util.Base64
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import com.appmattus.certificatetransparency.loglist.LogListDataSourceFactory
import nl.rijksoverheid.ctr.holder.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import retrofit2.Retrofit
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderApiClientUtil {
    fun client(certificateBytes: List<ByteArray>): HolderApiClient
}

class HolderApiClientUtilImpl(
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit,
) : HolderApiClientUtil {

    private fun transparentTrustManager(trustManager: X509TrustManager)  = certificateTransparencyTrustManager(trustManager) {
        println("transparentTrustManager")
        setLogger(object: CTLogger {
            override fun log(host: String, result: VerificationResult) {
                println("log $host $result")
            }
        })

        setLogListService(LogListDataSourceFactory.createLogListService())
    }

    override fun client(certificateBytes: List<ByteArray>): HolderApiClient {
        val okHttpClient = okHttpClient
            .newBuilder()
            .apply {
                if (BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .apply {
                            certificateBytes.forEach {
                                val base64Decoded = Base64.decode(it, Base64.DEFAULT)
                                val certificateFactory = CertificateFactory.getInstance("X.509")
                                val x509Certificate = certificateFactory.generateCertificate(
                                    ByteArrayInputStream(base64Decoded)
                                ) as X509Certificate
                                addTrustedCertificate(x509Certificate)
                            }
                        }
                        .build()

                    println("hello1")
                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        transparentTrustManager(handshakeCertificates.trustManager)
                    )
                }
            }.build()

        println("hello2")
        return retrofit.newBuilder().client(okHttpClient)
            .build().create(HolderApiClient::class.java)
    }
}