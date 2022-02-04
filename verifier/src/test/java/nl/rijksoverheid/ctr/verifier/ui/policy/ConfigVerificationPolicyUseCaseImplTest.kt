package nl.rijksoverheid.ctr.verifier.ui.policy

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.*
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import org.junit.Test
import kotlin.test.assertEquals

class ConfigVerificationPolicyUseCaseImplTest {
    private val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
    private val persistenceManager = mockk<PersistenceManager>(relaxed = true)
    private val verifierDatabase: VerifierDatabase = mockk(relaxed = true)

    private val configVerificationPolicyUseCase =
        ConfigVerificationPolicyUseCaseImpl(
            cachedAppConfigUseCase, persistenceManager,
            verifierDatabase
        )

    @Test
    fun `when policy from config changes, the selected policy should be cleared`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify { persistenceManager.removeVerificationPolicySelectionSet() }
    }

    @Test
    fun `when policy doesn't change, don't clear the selected policy`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G, VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify(exactly = 0) { persistenceManager.removeVerificationPolicySelectionSet() }
    }

    @Test
    fun `when policy is only 1G, set the policy to 1G`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy1G) }
    }

    @Test
    fun `when policy is only 3G, set the policy to 3G`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy3G) }
    }

    @Test
    fun `persist currently enabled policies on change`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G, VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify { persistenceManager.setEnabledPolicies(listOf(VerificationPolicy3G)) }
    }

    @Test
    fun `set new policy rules seen to false when new policy contains 1G`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify { persistenceManager.setNewPolicyRulesSeen(false) }
    }

    @Test
    fun `don't set new policy rules seen when policy doesn't change`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G, VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        verify(exactly = 0) { persistenceManager.setNewPolicyRulesSeen(any()) }
    }

    @Test
    fun `when policy from config changes, policy updated should be given`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        assertEquals(true, configVerificationPolicyUseCase.updatePolicy())
    }

    @Test
    fun `when policy from config doesn't change, policy not updated should be given`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G, VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        assertEquals(false, configVerificationPolicyUseCase.updatePolicy())
    }

    @Test
    fun `scan log should be cleared when policy from config changes`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        coVerify { verifierDatabase.scanLogDao().deleteAll() }
    }

    @Test
    fun `scan log should not be cleared when policy stays the same`() = runBlocking {
        every { persistenceManager.getEnabledPolicies() } returns listOf(VerificationPolicy3G, VerificationPolicy1G)
        every { cachedAppConfigUseCase.getCachedAppConfig() } returns VerifierConfig.default(
            policiesEnabled = listOf("3G", "1G")
        )

        configVerificationPolicyUseCase.updatePolicy()

        coVerify(exactly = 0) { verifierDatabase.scanLogDao().deleteAll() }
    }
}