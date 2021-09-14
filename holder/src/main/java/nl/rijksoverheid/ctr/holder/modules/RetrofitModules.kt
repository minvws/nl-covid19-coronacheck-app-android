package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.api.signing.certificates.DIGICERT_BTC_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.ROOT_CA_G3
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.HolderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun retrofitModule(baseUrl: String) = module {
    single {
        val okHttpClient = get<OkHttpClient>(OkHttpClient::class)
            .newBuilder()
            .apply {
                if (BuildConfig.FEATURE_TEST_PROVIDER_API_CHECKS) {
                    val handshakeCertificates = HandshakeCertificates.Builder()
                        .addTrustedCertificate(ROOT_CA_G3.decodeCertificatePem())
                        .addTrustedCertificate(EV_ROOT_CA.decodeCertificatePem())
                        .addTrustedCertificate(PRIVATE_ROOT_CA.decodeCertificatePem())
                        .addTrustedCertificate(DIGICERT_BTC_ROOT_CA.decodeCertificatePem())
                        .build()

                    sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(),
                        handshakeCertificates.trustManager
                    )
                }
            }.build()

        Retrofit.Builder()
            .client(okHttpClient)
            // required, although not used for TestProviders
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(TestProviderApiClient::class.java)
    }

    single {
        get<Retrofit>(Retrofit::class).create(HolderApiClient::class.java)
    }
}
