package nl.rijksoverheid.ctr.shared.models

import android.content.Context

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class Environment {
    object Acc: Environment()
    object Prod: Environment()

    companion object {
        fun get(context: Context): Environment {
            return if (context.packageName.contains(".acc")) {
                Acc
            } else {
                Prod
            }
        }
    }
}
