package nl.rijksoverheid.ctr.holder

import nl.rijksoverheid.ctr.shared.models.Step

sealed class HolderStep(override val code: Int) : Step(code) {
    object ConfigurationNetworkRequest: HolderStep(10)
    object DigidNetworkRequest: HolderStep(10)
    object CouplingNetworkRequest: HolderStep(10)
    object PublicKeysNetworkRequest: HolderStep(20)
    object TestProvidersNetworkRequest: HolderStep(20)
    object TestResultNetworkRequest: HolderStep(50)
    object ConfigProvidersNetworkRequest: HolderStep(20)
    object AccessTokensNetworkRequest: HolderStep(30)
    object UnomiNetworkRequest: HolderStep(40)
    object EventNetworkRequest: HolderStep(50)
    object StoringEvents: HolderStep(60)
    object PrepareIssueNetworkRequest: HolderStep(70)
    object GetCredentialsNetworkRequest: HolderStep(80)
    object StoringCredentials: HolderStep(90)
}