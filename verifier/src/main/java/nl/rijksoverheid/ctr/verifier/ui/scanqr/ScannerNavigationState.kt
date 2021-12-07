package nl.rijksoverheid.ctr.verifier.ui.scanqr


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ScannerNavigationState {
    data class Scanner(val isLocked: Boolean): ScannerNavigationState()
    object Instructions: ScannerNavigationState()
    object VerificationPolicySelection: ScannerNavigationState()
}