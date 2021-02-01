package nl.rijksoverheid.ctr.citizen

import androidx.preference.PreferenceManager
import nl.rijksoverheid.ctr.citizen.persistence.PersistenceManager
import nl.rijksoverheid.ctr.citizen.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.citizen.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.citizen.repositories.CitizenRepository
import nl.rijksoverheid.ctr.citizen.usecases.*
import org.koin.android.ext.koin.androidContext
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

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            PreferenceManager.getDefaultSharedPreferences(
                androidContext()
            )
        )
    }

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
            get(),
            get(),
            get()
        )
    }
    single {
        SecretKeyUseCase(get())
    }
    single {
        CommitmentMessageUseCase(get())
    }

    // ViewModels
    viewModel { CitizenViewModel(get(), get()) }

    // Repositories
    single { AuthenticationRepository() }
    single { CitizenRepository(get()) }
}
