package nl.rijksoverheid.ctr.introduction

import androidx.preference.PreferenceManager
import nl.rijksoverheid.ctr.holder.persistence.IntroductionPersistenceManager
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

val introductionModule = module {
    single {
        IntroductionPersistenceManager(
            PreferenceManager.getDefaultSharedPreferences(
                androidContext(),
            )
        )
    }
    viewModel<IntroductionViewModel> { IntroductionViewModelImpl(get()) }
}
