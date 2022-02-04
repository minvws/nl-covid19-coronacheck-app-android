package nl.rijksoverheid.ctr.design

import nl.rijksoverheid.ctr.design.utils.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val designModule = module {
    factory<DialogUtil> { DialogUtilImpl() }
    factory<InfoFragmentUtil> { InfoFragmentUtilImpl() }
    factory<IntentUtil> { IntentUtilImpl(get()) }

    viewModel<DialogFragmentViewModel> {
        DialogFragmentViewModelImpl()
    }
}
