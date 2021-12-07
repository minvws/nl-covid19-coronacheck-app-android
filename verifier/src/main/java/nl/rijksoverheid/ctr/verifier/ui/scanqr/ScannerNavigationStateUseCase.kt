package nl.rijksoverheid.ctr.verifier.ui.scanqr

import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScannerNavigationStateUseCase {
    fun get(): ScannerNavigationState
}

class ScannerNavigationStateUseCaseImpl(
    private val persistenceManager: PersistenceManager,
): ScannerNavigationStateUseCase {
    override fun get(): ScannerNavigationState {
        return if (!persistenceManager.getScanInstructionsSeen()) {
            ScannerNavigationState.Instructions
        } else if (!persistenceManager.isVerificationPolicySelectionSet()) {
            ScannerNavigationState.VerificationPolicySelection
        } else {
            ScannerNavigationState.Scanner
        }
    }
}
