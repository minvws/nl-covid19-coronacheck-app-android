package nl.rijksoverheid.ctr.holder.myoverview.models

import nl.rijksoverheid.ctr.holder.models.LocalTestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class LocalTestResultState {
    data class Valid(val localTestResult: LocalTestResult) : LocalTestResultState()
    object Expired : LocalTestResultState()
    object None : LocalTestResultState()
}
