package nl.rijksoverheid.ctr.verifier.modules

import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val storageModule = module {
    single {
        VerifierDatabase.createInstance(
            androidContext(),
            get(),
            androidContext().packageName == "nl.rijksoverheid.ctr.verifier"
        )
    }
}