package nl.rijksoverheid.ctr.verifier

import nl.rijksoverheid.ctr.verifier.usecases.DecryptHolderQrUseCase
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val verifierModule = module {
    single {
        DecryptHolderQrUseCase()
    }

    // ViewModels
    viewModel { VerifierViewModel(get()) }
}
