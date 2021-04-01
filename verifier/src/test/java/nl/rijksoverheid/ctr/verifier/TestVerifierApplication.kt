package nl.rijksoverheid.ctr.verifier

import androidx.preference.PreferenceManager
import org.koin.core.module.Module
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestVerifierApplication : VerifierApplication() {
    override fun getAdditionalModules(): List<Module> {
        return listOf(module {
            factory {
                PreferenceManager.getDefaultSharedPreferences(this@TestVerifierApplication)
            }
        })
    }
}
