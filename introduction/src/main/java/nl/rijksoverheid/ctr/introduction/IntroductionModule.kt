package nl.rijksoverheid.ctr.introduction

import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCaseImpl
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
    factory {
        IntroductionPersistenceManager(
            get()
        )
    }
    factory<IntroductionStatusUseCase> { IntroductionStatusUseCaseImpl(get(), get()) }
    viewModel<IntroductionViewModel> { IntroductionViewModelImpl(get(), get()) }
}
