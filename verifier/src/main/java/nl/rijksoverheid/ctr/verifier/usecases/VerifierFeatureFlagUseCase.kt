/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.usecases

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase

interface VerifierFeatureFlagUseCase {
    fun isVerificationPolicySelectionEnabled(): Boolean
}

class VerifierFeatureFlagUseCaseImpl(
    private val appConfigUseCase: VerifierCachedAppConfigUseCase,
): VerifierFeatureFlagUseCase {

    override fun isVerificationPolicySelectionEnabled(): Boolean {
        val verificationPoliciesEnabled = appConfigUseCase.getCachedAppConfig().verificationPolicies
            .filter { VerificationPolicy.fromConfigValue(it) != null }
        return verificationPoliciesEnabled.size > 1
    }
}