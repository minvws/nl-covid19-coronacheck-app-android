package nl.rijksoverheid.ctr.holder.modules

import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import nl.rijksoverheid.ctr.api.signing.certificates.DIGICERT_BTC_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.ROOT_CA_G3
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCaseImpl
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.ui.create_qr.TestResultsViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.TokenQrViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.VaccinationViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.VaccinationViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.HolderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.RemoteEventsStatusJsonAdapter
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.RemoteTestStatusJsonAdapter
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.ResponseError
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.LocalTestResultViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.LocalTestResultViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.LocalTestResultUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.decodeCertificatePem
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun holderModule(baseUrl: String) = module {

    single {
        Room
            .databaseBuilder(androidContext(), HolderDatabase::class.java, "holder-database")
            .createFromAsset("database/holder-database.db")
            .build()
    }

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get()
        )
    }

    // Use cases
    single {
        GenerateHolderQrCodeUseCase(get())
    }
    factory<QrCodeUseCase> {
        QrCodeUseCaseImpl(
            get(),
            get(),
        )
    }
    factory<SecretKeyUseCase> {
        SecretKeyUseCaseImpl(get())
    }
    factory<CommitmentMessageUseCase> {
        CommitmentMessageUseCaseImpl(get())
    }
    factory<ConfigProvidersUseCase> {
        ConfigProvidersUseCaseImpl(get())
    }
    factory {
        TestResultUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory<LocalTestResultUseCase> {
        LocalTestResultUseCaseImpl(get(), get(), get(), get(), get())
    }
    factory<TokenValidatorUtil> { TokenValidatorUtilImpl() }
    factory {
        TokenQrUseCase(get())
    }
    factory<DeviceRootedUseCase> { DeviceRootedUseCaseImpl(androidContext()) }
    factory<EventUseCase> { EventUseCaseImpl(get(), get(), get()) }

    // ViewModels
    viewModel<LocalTestResultViewModel> { LocalTestResultViewModelImpl(get(), get()) }
    viewModel { DigiDViewModel(get()) }
    viewModel { TestResultsViewModelImpl(get(), get(), get(), get()) }
    viewModel { TokenQrViewModel(get()) }
    viewModel<DeviceRootedViewModel> { DeviceRootedViewModelImpl(get(), get()) }
    viewModel<VaccinationViewModel> { VaccinationViewModelImpl(get()) }

    // Repositories
    single { AuthenticationRepository() }
    factory<CoronaCheckRepository> {
        CoronaCheckRepositoryImpl(
            get(),
            get(named("ResponseError"))
        )
    }
    factory<TestProviderRepository> {
        TestProviderRepositoryImpl(
            get(),
            get(named("SignedResponseWithModel"))
        )
    }
    factory<EventProviderRepository> {
        EventProviderRepositoryImpl(
            get()
        )
    }

    // Utils
    factory<QrCodeUtil> { QrCodeUtilImpl() }
    factory<TestResultAdapterItemUtil> { TestResultAdapterItemUtilImpl(get()) }

    // Usecases
    factory<CreateCredentialUseCase> {
        CreateCredentialUseCaseImpl()
    }

    factory<TestResultAttributesUseCase> {
        TestResultAttributesUseCaseImpl(get())
    }

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

    single<Converter<ResponseBody, SignedResponseWithModel<RemoteTestResult>>>(named("SignedResponseWithModel")) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            Types.newParameterizedType(
                SignedResponseWithModel::class.java,
                RemoteTestResult::class.java
            ), emptyArray()
        )
    }

    single<Converter<ResponseBody, ResponseError>>(named("ResponseError")) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            ResponseError::class.java, emptyArray()
        )
    }

    single {
        get<Moshi.Builder>(Moshi.Builder::class)
            .add(RemoteTestStatusJsonAdapter())
            .add(RemoteEventsStatusJsonAdapter())
            .build()
    }
}
