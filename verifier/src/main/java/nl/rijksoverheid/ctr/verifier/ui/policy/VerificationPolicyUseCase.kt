package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy

interface VerificationPolicyUseCase {
    fun get(): VerificationPolicy
    fun store()
}