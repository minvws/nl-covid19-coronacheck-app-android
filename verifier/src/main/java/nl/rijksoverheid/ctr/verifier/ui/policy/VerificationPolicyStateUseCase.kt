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
interface VerificationPolicyStateUseCase {
    fun get(): VerificationPolicyState
}

class VerificationPolicyStateUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val featureFlagUseCase: FeatureFlagUseCase
): VerificationPolicyStateUseCase {

    override fun get(): VerificationPolicyState {
        return if (featureFlagUseCase.isVerificationPolicyEnabled()) {
            when (persistenceManager.getVerificationPolicySelected()) {
                VerificationPolicy.VerificationPolicy2G -> VerificationPolicyState.Policy2G
                VerificationPolicy.VerificationPolicy3G -> VerificationPolicyState.Policy3G
                else -> VerificationPolicyState.None
            }
        } else {
            VerificationPolicyState.None
        }
    }
}
