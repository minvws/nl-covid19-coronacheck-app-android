package nl.rijksoverheid.ctr.shared.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.koin.android.ext.android.getDefaultScope
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ViewModelOwnerDefinition
import org.koin.androidx.viewmodel.scope.BundleDefinition
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
inline fun <reified T : ViewModel> Fragment.sharedViewModelWithOwner(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
    noinline state: BundleDefinition? = null,
    noinline owner: ViewModelOwnerDefinition = {
        ViewModelOwner.from(
            requireActivity()
        )
    }
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        getDefaultScope().getViewModel(qualifier, state, owner, T::class, parameters)
    }
}
