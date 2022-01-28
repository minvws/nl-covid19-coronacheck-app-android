package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface VerificationPolicySelectionStateUseCase {
    fun get(): VerificationPolicySelectionState
}

class VerificationPolicySelectionStateUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val featureFlagUseCase: FeatureFlagUseCase
) : VerificationPolicySelectionStateUseCase {

    /**
     * if set, get the user selected verification policy or none otherwise
     */
    override fun get(): VerificationPolicySelectionState {
        return when {
            featureFlagUseCase.isVerificationPolicySelectionEnabled() -> {
                when (persistenceManager.getVerificationPolicySelected()) {
                    VerificationPolicy.VerificationPolicy3G -> VerificationPolicySelectionState.Selection.Policy3G
                    VerificationPolicy.VerificationPolicy1G -> VerificationPolicySelectionState.Selection.Policy1G
                    else -> VerificationPolicySelectionState.None
                }
            }
            featureFlagUseCase.is1GOnlyEnabled() -> {
                VerificationPolicySelectionState.Only1G
            }
            else -> {
                VerificationPolicySelectionState.None
            }
        }
    }
}
