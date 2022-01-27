package nl.rijksoverheid.ctr.verifier.ui.policy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import org.junit.Assert.*
import org.junit.Test

class ConfigVerificationPolicyUseCaseImplTest {
    private val verificationPolicySelectionStateUseCase = mockk<VerificationPolicySelectionStateUseCase>()
    private val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
    private val persistenceManager = mockk<PersistenceManager>().apply {
        every { removeVerificationPolicySelectionSet() } returns Unit
    }

    private val configVerificationPolicyUseCase = ConfigVerificationPolicyUseCaseImpl(verificationPolicySelectionStateUseCase, cachedAppConfigUseCase, persistenceManager)

    @Test
    fun `1G config value returns 1G state`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } returns listOf("1G")

        val state = configVerificationPolicyUseCase.get()

        assertEquals(VerificationPolicySelectionState.Policy1G, state)
    }

    @Test
    fun `3G config value returns state None`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } returns listOf("3G")

        val state = configVerificationPolicyUseCase.get()

        assertEquals(VerificationPolicySelectionState.None, state)
    }

    @Test
    fun `more than one config values return the state selected by the user`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } returns listOf("1G", "3G")
        every { verificationPolicySelectionStateUseCase.get() } returns VerificationPolicySelectionState.Policy1G

        val state = configVerificationPolicyUseCase.get()

        assertEquals(VerificationPolicySelectionState.Policy1G, state)
    }

    @Test
    fun `if config has only one element, then reset policy selection`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } returns listOf("3G")

        configVerificationPolicyUseCase.get()

        verify { persistenceManager.removeVerificationPolicySelectionSet() }
    }
}