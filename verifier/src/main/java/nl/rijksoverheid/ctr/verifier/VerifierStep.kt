package nl.rijksoverheid.ctr.verifier

import nl.rijksoverheid.ctr.api.models.Step

sealed class VerifierStep(val code: Int) : Step {
    object ConfigurationNetworkRequest : VerifierStep(10)
    object PublicKeysNetworkRequest : VerifierStep(20)
}