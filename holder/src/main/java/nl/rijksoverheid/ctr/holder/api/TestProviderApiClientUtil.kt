package nl.rijksoverheid.ctr.holder.api

import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestProviderApiClientUtil {
    fun client(certificateBytes: List<ByteArray>): TestProviderApiClient
}

class TestProviderApiClientUtilImpl(
    private val moshi: Moshi,
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit,
) : TestProviderApiClientUtil {
    override fun client(certificateBytes: List<ByteArray>): TestProviderApiClient {
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
                                    ByteArrayInputStream(it)
                                ) as X509Certificate
                                addTrustedCertificate(x509Certificate)
                            }
                        }
                        .build()

                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
            }.build()

        return retrofit.newBuilder().client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build().create(TestProviderApiClient::class.java)
    }
}