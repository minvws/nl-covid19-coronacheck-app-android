package nl.rijksoverheid.ctr.shared.models

import android.content.Context
import android.os.Build

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class Environment {
    object Tst: Environment()
    object Acc: Environment()
    object Prod: Environment()
    object InstrumentationTests: Environment()

    companion object {
        fun get(context: Context): Environment {
            return with(context.packageName) {
                when {
                    contains(".test") || isRobolectricTest() -> InstrumentationTests
                    contains(".tst") -> Tst
                    contains(".acc") -> Acc
                    else -> Prod
                }
            }
        }

        private fun isRobolectricTest(): Boolean {
            return "robolectric" == Build.FINGERPRINT
        }
    }
}
