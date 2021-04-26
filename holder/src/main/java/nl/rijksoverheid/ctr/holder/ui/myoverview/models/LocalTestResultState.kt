package nl.rijksoverheid.ctr.holder.ui.myoverview.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class LocalTestResultState {
    data class Valid(val localTestResult: LocalTestResult, val firstTimeCreated: Boolean) : LocalTestResultState()
    object Expired : LocalTestResultState()
    object None : LocalTestResultState()
}
