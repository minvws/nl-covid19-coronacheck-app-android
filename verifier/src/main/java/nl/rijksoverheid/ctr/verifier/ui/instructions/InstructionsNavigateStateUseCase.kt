package nl.rijksoverheid.ctr.verifier.ui.instructions

import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface InstructionsNavigateStateUseCase {
    fun get(): InstructionsNavigateState
}

class InstructionsNavigateStateUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val scannerStateUseCase: ScannerStateUseCase,
    private val featureFlagUseCase: FeatureFlagUseCase,
): InstructionsNavigateStateUseCase {
    override fun get(): InstructionsNavigateState {
        return if (!persistenceManager.isVerificationPolicySelectionSet() && featureFlagUseCase.isVerificationPolicyEnabled()) {
            InstructionsNavigateState.VerificationPolicySelection
        } else {
            InstructionsNavigateState.Scanner(scannerStateUseCase.get() is ScannerState.Locked)
        }
    }
}