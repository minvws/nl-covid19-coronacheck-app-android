package nl.rijksoverheid.ctr.citizen

import nl.rijksoverheid.ctr.citizen.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.citizen.usecases.AllowedTestResultForEventUseCase
import nl.rijksoverheid.ctr.citizen.usecases.CitizenQrCodeUseCase
import nl.rijksoverheid.ctr.citizen.usecases.EventValidUseCase
import nl.rijksoverheid.ctr.citizen.usecases.GenerateCitizenQrCodeUseCase
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
// TODO: Make another sharedModule that shares logic between citizen and verifier
val citizenModule = module {
    // Use cases
    single {
        EventValidUseCase(
            get(),
            get()
        )
    }
    single {
        AllowedTestResultForEventUseCase(
            get()
        )
    }
    single {
        GenerateCitizenQrCodeUseCase(
            get(),
            get(),
            get()
        )
    }
    single {
        CitizenQrCodeUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // ViewModels
    viewModel { CitizenViewModel(get()) }

    // Repositories
    single { AuthenticationRepository() }
}
