package nl.rijksoverheid.ctr.shared.ext

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
inline fun clmobile.Result.successString(): String {
    if (this.error.isNotEmpty()) {
        throw Exception(this.error)
    } else {
        return String(this.value)
    }
}
