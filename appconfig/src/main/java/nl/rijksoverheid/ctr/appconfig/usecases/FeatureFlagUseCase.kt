/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.*

interface FeatureFlagUseCase {
    @Deprecated("this is used now only from the holder app and it will change in a forecoming holder ticket")
    fun isVerificationPolicyEnabled(): Boolean
    fun isVerificationPolicySelectionEnabled(): Boolean
}

class FeatureFlagUseCaseImpl(
    private val appConfigUseCase: CachedAppConfigUseCase,
): FeatureFlagUseCase {

    override fun isVerificationPolicyEnabled(): Boolean {
        val verificationPoliciesEnabled = appConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled
        return verificationPoliciesEnabled.contains(
            VerificationPolicy1G.configValue)
    }

    override fun isVerificationPolicySelectionEnabled(): Boolean {
        val verificationPoliciesEnabled = appConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled
            .filter { VerificationPolicy.fromConfigValue(it) != null }
        return verificationPoliciesEnabled.size > 1
    }
}