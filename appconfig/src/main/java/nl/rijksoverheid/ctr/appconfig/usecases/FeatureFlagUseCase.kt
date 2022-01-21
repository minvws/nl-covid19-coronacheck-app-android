/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import java.lang.IllegalStateException

interface FeatureFlagUseCase {
    fun isVerificationPolicyEnabled(): Boolean
    fun isVerificationPolicySelectionEnabled(): Boolean
    fun verificationPoliciesEnabled(): List<VerificationPolicy>
}

class FeatureFlagUseCaseImpl(
    private val buildConfigUseCase: BuildConfigUseCase,
    private val appConfigUseCase: CachedAppConfigUseCase,
): FeatureFlagUseCase {

    override fun isVerificationPolicyEnabled(): Boolean {
        return if (appConfigUseCase.getCachedAppConfig().enableVerificationPolicyVersion == 0) {
            false
        } else {
            buildConfigUseCase.getVersionCode() >= appConfigUseCase.getCachedAppConfig().enableVerificationPolicyVersion
        }
    }

    override fun isVerificationPolicySelectionEnabled(): Boolean {
        return appConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled.size > 1
    }

    override fun verificationPoliciesEnabled(): List<VerificationPolicy> {
        return appConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled.map {
            when (it) {
                "3G" -> VerificationPolicy.VerificationPolicy3G
                "2G" -> VerificationPolicy.VerificationPolicy2G
                "2GPlus" -> VerificationPolicy.VerificationPolicy2GPlus
                "1G" -> VerificationPolicy.VerificationPolicy3G
                else -> throw IllegalStateException("invalid policy value")
            }
        }
    }
}