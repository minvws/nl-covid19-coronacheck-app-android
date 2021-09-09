package nl.rijksoverheid.ctr.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.CacheOverrideInterceptor
import nl.rijksoverheid.ctr.api.interceptors.SignedResponseInterceptor
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.json.JsonObjectJsonAdapter
import nl.rijksoverheid.ctr.api.json.LocalDateJsonAdapter
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun apiModule(
    baseUrl: String,
    signatureCertificateCnMatch: String,
    coronaCheckApiChecks: Boolean,
    testProviderApiChecks: Boolean
) = module(override = true) {
//    test
//    Peer certificate chain:
//    sha256/ijPJtUxvyW65XLVzgoNUx9pjBIlSTgzZXbFhZ43mnW4=: CN=api-ct.bananenhalen.nl
//    sha256/jQJTbIh0grw0/1TkHSumWb+Fs0Ggogr621gT3PvPKG0=: CN=R3,O=Let's Encrypt,C=US
//    sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=: CN=ISRG Root X1,O=Internet Security Research Group,C=US
//    Pinned certificates for api-ct.bananenhalen.nl:

//    acc
//    Peer certificate chain:
//    sha256/7wUCSlYbr+pQ8wGizsQOK1NOCxldhmflswIkU5XqW9M=: CN=holder-api.acc.coronacheck.nl,O=Ministerie van Volksgezondheid\, Welzijn en Sport,L='s-Gravenhage,C=NL
//    sha256/Yao+RgzIlYNhXc65ch9IpKzSRFUSiL01Et8c6sN4XLU=: CN=KPN PKIoverheid Server CA 2020,O=KPN B.V.,C=NL
//    sha256/N9+YluTCUa/HTXc60QxjUReBLpRniAkIK2N84DhgmW4=: CN=Staat der Nederlanden Domein Server CA 2020,O=Staat der Nederlanden,C=NL
//    sha256/lR7gRvqDMW5nhsCMRPE7TKLq0tJkTWMxQ5HAzHCIfQ0=: CN=Staat der Nederlanden EV Root CA,O=Staat der Nederlanden,C=NL
//    Pinned certificates for holder-api.acc.coronacheck.nl:

    val url = URL(baseUrl)
//    prod
//    HTTP FAILED: javax.net.ssl.SSLPeerUnverifiedException: Certificate pinning failure!
//    Peer certificate chain:
//    sha256/WSn3YP84KZCric17vR8cTIyqgk+PIDntvMLYiBqOCN4=: CN=holder-api.coronacheck.nl,O=Ministerie van Volksgezondheid\, Welzijn en Sport,L='s-Gravenhage,C=NL
//    sha256/Yao+RgzIlYNhXc65ch9IpKzSRFUSiL01Et8c6sN4XLU=: CN=KPN PKIoverheid Server CA 2020,O=KPN B.V.,C=NL
//    sha256/N9+YluTCUa/HTXc60QxjUReBLpRniAkIK2N84DhgmW4=: CN=Staat der Nederlanden Domein Server CA 2020,O=Staat der Nederlanden,C=NL
//    sha256/lR7gRvqDMW5nhsCMRPE7TKLq0tJkTWMxQ5HAzHCIfQ0=: CN=Staat der Nederlanden EV Root CA,O=Staat der Nederlanden,C=NL
//    Pinned certificates for holder-api.coronacheck.nl:
//    sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=
    single {
        OkHttpClient.Builder()
            .addNetworkInterceptor(CacheOverrideInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)
            .certificatePinner(CertificatePinner.Builder()
                .add(url.host, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=").build()
            )
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor {
                        Timber.tag("OkHttp").d(it)
                    }.setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .addInterceptor(
                SignedResponseInterceptor(
                    signatureCertificateCnMatch = signatureCertificateCnMatch,
                    testProviderApiChecks = testProviderApiChecks
                )
            ).build()
    }

    single {
        val okHttpClient = get<OkHttpClient>(OkHttpClient::class)
            .newBuilder()
            .apply {
                if (coronaCheckApiChecks) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .addTrustedCertificate(EV_ROOT_CA.decodeCertificatePem())
                        .build()
                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
                if (!BuildConfig.DEBUG) {
                    connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
                }
            }
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single {
        Moshi.Builder()
            .add(Base64JsonAdapter())
            .add(JsonObjectJsonAdapter())
            .add(OffsetDateTimeJsonAdapter())
            .add(LocalDateJsonAdapter())
    }
}
