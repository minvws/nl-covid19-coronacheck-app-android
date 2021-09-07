package nl.rijksoverheid.ctr.holder.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.api.signing.certificates.DIGICERT_BTC_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.ROOT_CA_G3
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCaseImpl
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModelImpl
import nl.rijksoverheid.ctr.holder.persistence.*
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncerImpl
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManager
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManagerImpl
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofCodeViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofCodeViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofQrScannerViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofQrScannerViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.QrCodeViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.QrCodeViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.ReturnToExternalAppUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverViewGreenCardAdapterUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverViewGreenCardAdapterUtilImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.*
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactoryImpl
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
import java.time.Clock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun holderModule(baseUrl: String) = module {

    single {
        HolderDatabase.createInstance(androidContext(), get(), androidContext().packageName == "nl.rijksoverheid.ctr.holder")
    }

    factory<HolderDatabaseSyncer> { HolderDatabaseSyncerImpl(get(), get(), get(), get()) }

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get()
        )
    }

    // Use cases
    factory<PaperProofCodeUseCase> {
        PaperProofCodeUseCaseImpl()
    }
    factory<GetRemoteGreenCardsUseCase> {
        GetRemoteGreenCardsUseCaseImpl(get(), get(), get())
    }
    factory<SyncRemoteGreenCardsUseCase> {
        SyncRemoteGreenCardsUseCaseImpl(get(), get(), get(), get())
    }
    factory<CreateDomesticGreenCardUseCase> {
        CreateDomesticGreenCardUseCaseImpl(get())
    }
    factory<CreateEuGreenCardUseCase> {
        CreateEuGreenCardUseCaseImpl(get(), get())
    }
    factory<GetEventProvidersWithTokensUseCase> {
        GetEventProvidersWithTokensUseCaseImpl(get())
    }
    factory<GetRemoteEventsUseCase> {
        GetRemoteEventsUseCaseImpl(get())
    }
    factory<QrCodeUseCase> {
        QrCodeUseCaseImpl(
            get(),
            get(),
            get()
        )
    }
    factory<SecretKeyUseCase> {
        SecretKeyUseCaseImpl(get(), get())
    }
    factory<CommitmentMessageUseCase> {
        CommitmentMessageUseCaseImpl(get(), get())
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
        )
    }
    factory<GetMyOverviewItemsUseCase> {
        GetMyOverviewItemsUseCaseImpl(get(), get(), get(), get())
    }
    factory<MyOverViewGreenCardAdapterUtil> { MyOverViewGreenCardAdapterUtilImpl(androidContext(), get(), get(), get()) }
    factory<TokenValidatorUtil> { TokenValidatorUtilImpl() }
    factory<CredentialUtil> { CredentialUtilImpl(Clock.systemUTC(), get()) }
    factory<OriginUtil> { OriginUtilImpl(Clock.systemUTC()) }
    factory<RemoteEventRecoveryUtil> { RemoteEventRecoveryUtilImpl(get()) }
    factory<RemoteEventHolderUtil> { RemoteEventHolderUtilImpl(get(), get()) }
    factory<RemoteProtocol3Util> { RemoteProtocol3UtilImpl() }
    factory<RemoteEventUtil> { RemoteEventUtilImpl() }
    factory {
        TokenQrUseCase(get())
    }
    factory<DeviceRootedUseCase> { DeviceRootedUseCaseImpl(androidContext()) }
    factory<GetEventsUseCase> { GetEventsUseCaseImpl(get(), get(), get(), get()) }
    factory<SaveEventsUseCase> { SaveEventsUseCaseImpl(get(), get()) }
    factory<CachedAppConfigUseCase> { CachedAppConfigUseCaseImpl(get(), androidContext().filesDir.path, get()) }

    factory<TestResultsMigrationManager> { TestResultsMigrationManagerImpl(get()) }

    factory<WorkerManagerWrapper> { WorkerManagerWrapperImpl(androidContext(), get()) }

    // ViewModels
    viewModel<QrCodeViewModel> { QrCodeViewModelImpl(get(), get()) }
    viewModel<HolderMainActivityViewModel> { HolderMainActivityViewModelImpl() }
    viewModel<CommercialTestCodeViewModel> { CommercialTestCodeViewModelImpl(get(), get()) }
    viewModel { DigiDViewModel(get()) }
    viewModel { TokenQrViewModel(get()) }
    viewModel<DeviceRootedViewModel> { DeviceRootedViewModelImpl(get(), get()) }
    viewModel<YourEventsViewModel> { YourEventsViewModelImpl(get(), get()) }
    viewModel<MyOverviewViewModel> { MyOverviewViewModelImpl(get(), get(), get(), get(), get(), get()) }
    viewModel<MyOverviewTabsViewModel> { MyOverviewTabsViewModelImpl(get()) }
    viewModel<GetEventsViewModel> { GetEventsViewModelImpl(get()) }
    viewModel<PaperProofCodeViewModel> { PaperProofCodeViewModelImpl(get(), get()) }
    viewModel<PaperProofQrScannerViewModel> { PaperProofQrScannerViewModelImpl(get()) }

    // Repositories
    single { AuthenticationRepository() }
    factory<CoronaCheckRepository> {
        CoronaCheckRepositoryImpl(
            get(),
            get()
        )
    }
    factory<TestProviderRepository> {
        TestProviderRepositoryImpl(
            get(),
            get(),
            get(named("SignedResponseWithModel")),
        )
    }
    factory<EventProviderRepository> {
        EventProviderRepositoryImpl(
            get(),
            get(),
        )
    }

    // Utils
    factory<QrCodeUtil> { QrCodeUtilImpl() }
    factory<TestResultAdapterItemUtil> { TestResultAdapterItemUtilImpl(get()) }
    factory<InfoScreenUtil> { InfoScreenUtilImpl(get(), get(), get()) }
    factory<VaccinationInfoScreenUtil> {
        VaccinationInfoScreenUtilImpl(get(), androidContext().resources, get())
    }
    factory<LastVaccinationDoseUtil> { LastVaccinationDoseUtilImpl(androidContext().resources) }
    factory<GreenCardUtil> { GreenCardUtilImpl(Clock.systemUTC(), get()) }

    // Usecases
    factory<ValidatePaperProofUseCase> {
        ValidatePaperProofUseCaseImpl(get(), get())
    }

    factory<GetEventsFromPaperProofQrUseCase> {
        GetEventsFromPaperProofQrUseCaseImpl(get())
    }

    factory<CreateCredentialUseCase> {
        CreateCredentialUseCaseImpl(get())
    }

    factory<QrCodeDataUseCase> { QrCodeDataUseCaseImpl(get(), get(), get()) }

    factory<TestResultAttributesUseCase> {
        TestResultAttributesUseCaseImpl(get(), get())
    }

    factory<ReturnToExternalAppUseCase> {
        ReturnToExternalAppUseCaseImpl(get())
    }

    factory<RemoveExpiredEventsUseCase> {
        RemoveExpiredEventsUseCaseImpl(Clock.systemUTC(), get(), get())
    }

    factory<GreenCardRefreshUtil> {
        GreenCardRefreshUtilImpl(get(), get(), get(), get(), get())
    }

    factory {
        HolderWorkerFactory(get(), get())
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

    single<Converter<ResponseBody, SignedResponseWithModel<RemoteProtocol>>>(named("SignedResponseWithModel")) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            Types.newParameterizedType(
                SignedResponseWithModel::class.java,
                RemoteProtocol::class.java
            ), emptyArray()
        )
    }

    single<Converter<ResponseBody, CoronaCheckErrorResponse>>(named("ResponseError")) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            CoronaCheckErrorResponse::class.java, emptyArray()
        )
    }

    factory {
        NetworkRequestResultFactory(get(named("ResponseError")))
    }

    factory<ErrorCodeStringFactory> {
        ErrorCodeStringFactoryImpl()
    }

    single {
        get<Moshi.Builder>(Moshi.Builder::class)
            .add(RemoteTestStatusJsonAdapter())
            .add(OriginTypeJsonAdapter())
            .add(RemoteCouplingStatusJsonAdapter())
            .add(PolymorphicJsonAdapterFactory.of(
                RemoteProtocol::class.java, "protocolVersion")
                .withSubtype(RemoteTestResult2::class.java, "2.0")
                .withSubtype(RemoteProtocol3::class.java, "3.0"))
            .add(PolymorphicJsonAdapterFactory.of(
                RemoteEvent::class.java, "type")
                .withSubtype(RemoteEventPositiveTest::class.java, "positivetest")
                .withSubtype(RemoteEventRecovery::class.java, "recovery")
                .withSubtype(RemoteEventNegativeTest::class.java, "negativetest")
                .withSubtype(RemoteEventVaccination::class.java, "vaccination"))
            .add(KotlinJsonAdapterFactory())
            .build()
    }
}
