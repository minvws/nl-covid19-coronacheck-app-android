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
    fun `when policy from config changes, the selected policy should be cleared`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("3G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.removeVerificationPolicySelectionSet() }
    }

    @Test
    fun `when policy doesn't change, don't clear the selected policy`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("3G", "1G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify(exactly = 0) { persistenceManager.removeVerificationPolicySelectionSet() }
    }

    @Test
    fun `when policy is only 1G, set the policy to 1G`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("1G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("1G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) }
    }

    @Test
    fun `when policy is only 3G, set the policy to 3G`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("3G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy3G) }
    }

    @Test
    fun `persist currently enabled policies`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("3G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setEnabledPolicies(listOf("3G")) }
    }

    @Test
    fun `set new policy rules seen to false when new policy contains 1G`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("1G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify { persistenceManager.setNewPolicyRulesSeen(false) }
    }

    @Test
    fun `don't set new policy rules seen when policy doesn't change`() {
        every { persistenceManager.getEnabledPolicies() } returns listOf("3G", "1G")
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.update()

        verify(exactly = 0) { persistenceManager.setNewPolicyRulesSeen(any()) }
    }
}