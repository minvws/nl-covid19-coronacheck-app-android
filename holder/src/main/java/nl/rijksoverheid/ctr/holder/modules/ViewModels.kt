package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModel
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val viewModels = module {
    viewModel<DeviceRootedViewModel> { DeviceRootedViewModelImpl(get(), get()) }
    viewModel<DeviceSecureViewModel> { DeviceSecureViewModelImpl(get(), get()) }
}
