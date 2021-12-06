package nl.rijksoverheid.ctr.verifier.ui.scanqr

import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface NextScannerScreenUseCase {
    fun get(): NextScannerScreenState
}

class NextScannerScreenUseCaseImpl(
    private val persistenceManager: PersistenceManager,
): NextScannerScreenUseCase {
    override fun get(): NextScannerScreenState {
        return if (!persistenceManager.getScanInstructionsSeen()) {
            NextScannerScreenState.Instructions
        } else if (!persistenceManager.isVerificationPolicySelectionSet()) {
            NextScannerScreenState.VerificationPolicySelection
        } else {
            NextScannerScreenState.Scanner
        }
    }
}
