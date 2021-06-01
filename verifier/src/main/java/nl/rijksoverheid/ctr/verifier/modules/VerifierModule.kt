/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.modules

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.appconfig.eu.api.EuPublicKeysApi
import nl.rijksoverheid.ctr.appconfig.eu.repositories.EuPublicKeysRepository
import nl.rijksoverheid.ctr.appconfig.eu.repositories.EuPublicKeysRepositoryImpl
import nl.rijksoverheid.ctr.appconfig.eu.usecases.EuPublicKeyUsecase
import nl.rijksoverheid.ctr.appconfig.eu.usecases.EuPublicKeyUsecaseImpl
import nl.rijksoverheid.ctr.appconfig.eu.usecases.PersistEuPublicKeysUsecase
import nl.rijksoverheid.ctr.appconfig.eu.usecases.PersistEuPublicKeysUsecaseImpl
import nl.rijksoverheid.ctr.verifier.VerifierConfigViewModel
import nl.rijksoverheid.ctr.verifier.VerifierConfigViewModelImpl
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModelImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers.VerifiedQrDataMapper
import nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers.VerifiedQrDataMapperImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCaseImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCaseImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.QrCodeUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.QrCodeUtilImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtilImpl
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModelImpl
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
/**
 * Configure app config dependencies
 *
 * @param path Path for the public keys api, for example "keys" to fetch the config from <baseurl>/keys/public_keys
 */
fun verifierModule(path: String) = module {

    factory<NewTerms?> {
        NewTerms(version = 1, true)
    }

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get()
        )
    }

    // Repositories
    factory<EuPublicKeysRepository> {
        EuPublicKeysRepositoryImpl(get())
    }

    // Use cases
    factory<VerifyQrUseCase> {
        VerifyQrUseCaseImpl(get())
    }
    factory<TestResultValidUseCase> {
        TestResultValidUseCaseImpl(get(), get(), get(), get())
    }
    factory<VerifiedQrDataMapper> { VerifiedQrDataMapperImpl(get()) }

    factory<EuPublicKeyUsecase> {
        EuPublicKeyUsecaseImpl(get(), get(), get())
    }

    factory<PersistEuPublicKeysUsecase> {
        PersistEuPublicKeysUsecaseImpl(androidContext().cacheDir, get())
    }

    // Utils
    factory<QrCodeUtil> { QrCodeUtilImpl(get()) }
    factory<ScannerUtil> { ScannerUtilImpl() }

    // ViewModels
    viewModel<ScanQrViewModel> { ScanQrViewModelImpl(get()) }
    viewModel<ScannerViewModel> { ScannerViewModelImpl(get()) }
    viewModel<VerifierConfigViewModel> { VerifierConfigViewModelImpl(get(), get(), get(), androidContext().cacheDir.path) }

    single {
        get<Moshi.Builder>(Moshi.Builder::class).build()
    }

    single {
        val okHttpClient = get<OkHttpClient>(OkHttpClient::class).newBuilder().build()
        val retrofit = get<Retrofit>(Retrofit::class)
        val baseUrl = retrofit.baseUrl().newBuilder().addPathSegments("$path/").build()
        retrofit.newBuilder().baseUrl(baseUrl).client(okHttpClient).build()
            .create(EuPublicKeysApi::class.java)
    }
}
