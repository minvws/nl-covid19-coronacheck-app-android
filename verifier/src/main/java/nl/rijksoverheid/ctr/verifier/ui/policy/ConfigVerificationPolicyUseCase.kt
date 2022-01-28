package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.*
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
    fun get(): VerificationPolicySelectionState
}

class ConfigVerificationPolicyUseCaseImpl(
    private val verificationPolicySelectionStateUseCase: VerificationPolicySelectionStateUseCase,
    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager,
): ConfigVerificationPolicyUseCase {
    /**
     * The verification policy state of the app can be determined in two ways:
     * 1. From what the user has selected in the [VerificationPolicySelectionFragment] (that is only if more than one policies are offered by the config)
     * 2. Directly from the config (if one and only one policy is offered by the config)
     */
    override fun get(): VerificationPolicySelectionState {
        val verificationPoliciesEnabled = cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled + listOf("1G")

        // make sure there is no selection stored if config value changed
        // (eg it was ["3G", "1G"] and user selected 3G and then the config value changed to ["1G"])
        if (verificationPoliciesEnabled.size == 1) {
            persistenceManager.removeVerificationPolicySelectionSet()
        }

        return when {
            verificationPoliciesEnabled.size == 1 && verificationPoliciesEnabled.first() == VerificationPolicy1G.configValue -> VerificationPolicySelectionState.Policy1G
            verificationPoliciesEnabled.size > 1 -> verificationPolicySelectionStateUseCase.get()
            else -> VerificationPolicySelectionState.None
        }
    }
}