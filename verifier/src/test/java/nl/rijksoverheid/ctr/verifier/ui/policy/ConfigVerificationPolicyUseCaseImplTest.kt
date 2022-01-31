package nl.rijksoverheid.ctr.verifier.ui.policy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import org.junit.Test

class ConfigVerificationPolicyUseCaseImplTest {
    private val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
    private val persistenceManager = mockk<PersistenceManager>(relaxed = true)

    private val configVerificationPolicyUseCase =
        ConfigVerificationPolicyUseCaseImpl(cachedAppConfigUseCase, persistenceManager)

    @Test
    fun `when policy from config changes from not selectable to selectable, the selected policy should be cleared`() {
        every { persistenceManager.getIsPolicySelectable() } returns false
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.removeVerificationPolicySelectionSet() }
    }

    @Test
    fun `when policy is selectable and stays selectable, don't clear the selected policy`() {
        every { persistenceManager.getIsPolicySelectable() } returns true
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify(exactly = 0) { persistenceManager.removeVerificationPolicySelectionSet() }
    }

    @Test
    fun `when policy is only 1G, set the policy to 1G`() {
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("1G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) }
    }

    @Test
    fun `when policy is only 3G, set the policy to 3G`() {
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy3G) }
    }

    @Test
    fun `persist policy is not selectable when there is only 1 policy`() {
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setIsPolicySelectable(false) }
    }

    @Test
    fun `persist policy is selectable when there are multiple policies`() {
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setIsPolicySelectable(true) }
    }
}