package nl.rijksoverheid.ctr.holder.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class TestIsmResult {
    data class Success(val body: String) : TestIsmResult()
    data class Error(val responseError: nl.rijksoverheid.ctr.holder.models.ResponseError) : TestIsmResult()
}
