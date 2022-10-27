package nl.rijksoverheid.ctr.holder.fuzzy_matching

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
val fuzzyMatchingModule = module {
    viewModel {
        FuzzyMatchingOnboardingViewModel(get(), get())
    }

    viewModel<HolderNameSelectionViewModel> { (matchingBlobIds: List<List<Int>>) ->
        HolderNameSelectionViewModelImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            matchingBlobIds
        )
    }

    factory<SelectionDataUtil> {
        SelectionDataUtilImpl(
            get(),
            get(),
            get(),
            androidContext().resources::getQuantityString,
            androidContext()::getString
        )
    }

    factory<SelectionDetailBottomSheetDescriptionUtil> {
        SelectionDetailBottomSheetDescriptionUtilImpl()
    }

    factory<MatchedEventsUseCase> {
        MatchedEventsUseCaseImpl(get(), get())
    }
}
