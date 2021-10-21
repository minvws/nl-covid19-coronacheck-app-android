/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCaseImpl
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModelImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCaseImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCaseImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtilImpl
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

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

    // Use cases
    factory<VerifyQrUseCase> {
        VerifyQrUseCaseImpl(get())
    }
    factory<TestResultValidUseCase> {
        TestResultValidUseCaseImpl(get())
    }
    factory<ReturnToExternalAppUseCase> { ReturnToExternalAppUseCaseImpl(get()) }

    // Utils
    factory<ScannerUtil> { ScannerUtilImpl() }

    // ViewModels
    viewModel<ScanQrViewModel> { ScanQrViewModelImpl(get()) }
    viewModel<ScannerViewModel> { ScannerViewModelImpl(get(), get()) }

    single {
        get<Moshi.Builder>(Moshi.Builder::class)
            .add(KotlinJsonAdapterFactory()).build()
    }
}
