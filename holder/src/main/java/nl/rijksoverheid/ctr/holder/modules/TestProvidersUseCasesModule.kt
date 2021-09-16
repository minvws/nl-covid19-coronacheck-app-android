package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ConfigProvidersUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ConfigProvidersUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResultUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCaseImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val testProvidersUseCasesModule = module {
    factory<ConfigProvidersUseCase> {
        ConfigProvidersUseCaseImpl(get())
    }

    factory {
        TestResultUseCase(
            get(),
            get(),
            get(),
            get()
        )
    }

    factory<TestResultAttributesUseCase> {
        TestResultAttributesUseCaseImpl(get(), get())
    }
}
