package nl.rijksoverheid.ctr.shared.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class TestIsm {
    data class Success(val body: String) : TestIsm()
    data class Error(val responseError: ResponseError) : TestIsm()
}
