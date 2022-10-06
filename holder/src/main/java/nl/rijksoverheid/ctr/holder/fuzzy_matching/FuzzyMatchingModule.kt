package nl.rijksoverheid.ctr.holder.fuzzy_matching

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
    viewModel<HolderNameSelectionViewModel> { HolderNameSelectionViewModelImpl() }
}
