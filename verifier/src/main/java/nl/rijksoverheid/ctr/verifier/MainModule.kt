package nl.rijksoverheid.ctr.verifier

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.verifier.scanqr.ScanQrViewModel
import nl.rijksoverheid.ctr.verifier.usecases.DecryptHolderQrUseCase
import nl.rijksoverheid.ctr.verifier.usecases.TestResultValidUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val mainModule = module {

    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(
            androidContext(),
        )
    }

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get()
        )
    }

    // Use cases
    single {
        DecryptHolderQrUseCase(get())
    }
    single {
        TestResultValidUseCase(get(), get(), get(), get())
    }

    // ViewModels
    viewModel { ScanQrViewModel(get(), get()) }
}
