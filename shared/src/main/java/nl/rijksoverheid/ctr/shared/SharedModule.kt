package nl.rijksoverheid.ctr.shared

import android.content.Context
import java.time.Clock
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.shared.utils.AndroidUtilImpl
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtilImpl
import nl.rijksoverheid.ctr.shared.utils.TestResultUtil
import nl.rijksoverheid.ctr.shared.utils.TestResultUtilImpl
import nl.rijksoverheid.ctr.shared.utils.WebsiteEnvironmentUtil
import nl.rijksoverheid.ctr.shared.utils.WebsiteEnvironmentUtilImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

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
    factory<WebsiteEnvironmentUtil> { WebsiteEnvironmentUtilImpl(androidContext()) }
    factory<DebugDisclosurePolicyPersistenceManager> { DebugDisclosurePolicyPersistenceManagerImpl(get()) }
}

fun isHolderApp(applicationContext: Context): Boolean =
    applicationContext.packageName.contains("holder")
