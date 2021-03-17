package nl.rijksoverheid.ctr.shared

import nl.rijksoverheid.ctr.shared.usecase.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.shared.usecase.TestResultAttributesUseCaseImpl
import nl.rijksoverheid.ctr.shared.util.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.time.Clock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

val sharedModule = module {

    single { Clock.systemDefaultZone() }

    // Usecases
    factory<TestResultAttributesUseCase> {
        TestResultAttributesUseCaseImpl(get())
    }

    // Utils
    single { QrCodeUtil(get()) }
    factory<TestResultUtil> { TestResultUtilImpl(get()) }
    factory<PersonalDetailsUtil> {
        PersonalDetailsUtilImpl(
            passportMonths = androidContext().resources.getStringArray(
                R.array.passport_months
            ).toList()
        )
    }
}
