package nl.rijksoverheid.ctr.verifier.modules

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.introduction.models.NewTerms
import nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers.VerifiedQrDataMapper
import nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers.VerifiedQrDataMapperImpl
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModelImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtilImpl
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModelImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCaseImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCaseImpl
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.QrCodeUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.QrCodeUtilImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val verifierModule = module {

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
        TestResultValidUseCaseImpl(get(), get(), get(), get())
    }
    factory<VerifiedQrDataMapper> { VerifiedQrDataMapperImpl(get()) }

    // Utils
    factory<QrCodeUtil> { QrCodeUtilImpl(get()) }
    factory<ScannerUtil> { ScannerUtilImpl() }

    // ViewModels
    viewModel<ScanQrViewModel> { ScanQrViewModelImpl(get()) }
    viewModel<ScannerViewModel> { ScannerViewModelImpl(get()) }

    single {
        get(Moshi.Builder::class).build()
    }
}
