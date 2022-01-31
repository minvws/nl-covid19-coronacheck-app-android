package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy1G
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.VerificationPolicy3G
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ConfigVerificationPolicyUseCase {
    fun update()
}

class ConfigVerificationPolicyUseCaseImpl(
    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager,
) : ConfigVerificationPolicyUseCase {

    /**
     * The verification policy state of the app can be determined in two ways:
     * 1. From what the user has selected in the [VerificationPolicySelectionFragment] (that is only if more than one policies are offered by the config)
     * 2. Directly from the config (if one and only one policy is offered by the config)
     */
    override fun update() {
        val verificationPoliciesEnabled =
            cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled

        // Reset policy set when settings change from 1 policy to multiple selectable policies
        if (!persistenceManager.getIsPolicySelectable() && verificationPoliciesEnabled.size > 1) {
            persistenceManager.removeVerificationPolicySelectionSet()
        }

        // Set selection policy on single policy
        if (verificationPoliciesEnabled.size == 1) {
            when (verificationPoliciesEnabled.first()) {
                VerificationPolicy1G.configValue -> persistenceManager.setVerificationPolicySelected(VerificationPolicy1G)
                VerificationPolicy3G.configValue -> persistenceManager.setVerificationPolicySelected(VerificationPolicy3G)
            }
        }

        // Store current config setting whether policy setting is selectable
        persistenceManager.setIsPolicySelectable(verificationPoliciesEnabled.size > 1)
    }
}