package nl.rijksoverheid.ctr.holder.api

import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import com.appmattus.certificatetransparency.loglist.LogListDataSourceFactory
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
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
interface TestProviderApiClientUtil {
    fun client(
        tlsCertificateBytes: List<ByteArray>,
        cmsCertificateBytes: List<ByteArray>
    ): TestProviderApiClient
}

class TestProviderApiClientUtilImpl(
    private val moshi: Moshi,
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit,
) : TestProviderApiClientUtil {

    private fun transparentTrustManager(trustManager: X509TrustManager) =
        certificateTransparencyTrustManager(trustManager) {
            if (BuildConfig.DEBUG) {
                setLogger(object : CTLogger {
                    override fun log(host: String, result: VerificationResult) {
                        Timber.tag("certificate transparency")
                            .d("host: $host, verification result: $result")
                    }
                })
            }

            setLogListService(LogListDataSourceFactory.createLogListService())
        }

    override fun client(
        tlsCertificateBytes: List<ByteArray>,
        cmsCertificateBytes: List<ByteArray>
    ): TestProviderApiClient {
        val okHttpClient = okHttpClient
            .newBuilder()
            .apply {
                if (BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .apply {
                            val certificateBytes = tlsCertificateBytes + cmsCertificateBytes
                            certificateBytes.forEach {
                                val certificateFactory = CertificateFactory.getInstance("X.509")
                                val x509Certificate = certificateFactory.generateCertificate(
                                    ByteArrayInputStream(it)
                                ) as X509Certificate
                                addTrustedCertificate(x509Certificate)
                            }
                        }
                        .build()

                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        transparentTrustManager(handshakeCertificates.trustManager)
                    )
                }
            }.build()

        return retrofit.newBuilder().client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build().create(TestProviderApiClient::class.java)
    }
}