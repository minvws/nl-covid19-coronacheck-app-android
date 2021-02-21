package nl.rijksoverheid.ctr.api.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class ResponseError(
    val status: String,
    val code: Int
) {
    companion object {
        const val CODE_ALREADY_SIGNED = 99994
    }
}
