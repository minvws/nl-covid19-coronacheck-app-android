package nl.rijksoverheid.ctr.verifier

import nl.rijksoverheid.ctr.shared.error.Step

sealed class VerifierStep(override val code: Int) : Step(code) {
    object ConfigurationNetworkRequest : VerifierStep(10)
    object PublicKeysNetworkRequest : VerifierStep(20)
}