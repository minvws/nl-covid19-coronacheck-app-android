package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy1G
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy3G
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ConfigVerificationPolicyUseCase {
    suspend fun updatePolicy(): Boolean
}

class ConfigVerificationPolicyUseCaseImpl(
    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager,
    private val verifierDatabase: VerifierDatabase
) : ConfigVerificationPolicyUseCase {

    /**
     * The verification policy state of the app can be determined in two ways:
     * 1. From what the user has selected in the [VerificationPolicySelectionFragment] (that is only if more than one policies are offered by the config)
     * 2. Directly from the config (if one and only one policy is offered by the config)
     */
    override suspend fun updatePolicy(): Boolean {
        var policyUpdated = false

        val verificationPoliciesEnabled =
            cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled
                .filter { VerificationPolicy.fromConfigValue(it) != null }

        // Reset policy settings on policy change
        if (persistenceManager.getEnabledPolicies() != verificationPoliciesEnabled) {
            policyUpdated = true
            persistenceManager.removeVerificationPolicySelectionSet()
            verifierDatabase.scanLogDao().deleteAll()

            // When change contains 1G the new rules should be shown
            if (verificationPoliciesEnabled.contains(VerificationPolicy1G.configValue)) {
                persistenceManager.setNewPolicyRulesSeen(false)
            }

            // Store current config setting of enabled policies
            persistenceManager.setEnabledPolicies(verificationPoliciesEnabled)
        }

        // Set selection policy on single policy
        if (verificationPoliciesEnabled.size == 1) {
            when (verificationPoliciesEnabled.first()) {
                VerificationPolicy1G.configValue-> persistenceManager.setVerificationPolicySelected(
                    VerificationPolicy1G
                )
                else -> persistenceManager.setVerificationPolicySelected(
                    VerificationPolicy3G
                )
            }
        }

        return policyUpdated
    }
}