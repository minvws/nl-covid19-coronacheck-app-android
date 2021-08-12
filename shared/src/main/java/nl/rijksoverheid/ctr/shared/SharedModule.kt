package nl.rijksoverheid.ctr.shared

import nl.rijksoverheid.ctr.shared.utils.*
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

    // Utils
    factory<AndroidUtil> { AndroidUtilImpl(androidContext()) }
    factory<TestResultUtil> { TestResultUtilImpl(get()) }
    factory<PersonalDetailsUtil> {
        PersonalDetailsUtilImpl(
            passportMonths = androidContext().resources.getStringArray(
                R.array.passport_months
            ).toList()
        )
    }
    factory<IntentUtil> { IntentUtilImpl() }
}
