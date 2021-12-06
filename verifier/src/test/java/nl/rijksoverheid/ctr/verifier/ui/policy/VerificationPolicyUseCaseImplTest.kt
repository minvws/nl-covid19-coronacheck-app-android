package nl.rijksoverheid.ctr.verifier.ui.policy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class VerificationPolicyUseCaseImplTest {

    private val persistenceManager: PersistenceManager = mockk()

    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase = mockk()

    private val clock = Clock.fixed(Instant.parse("2021-12-01T00:00:00.00Z"), ZoneId.of("UTC"))
    private val useCase =
        VerificationPolicyUseCaseImpl(persistenceManager, clock, cachedAppConfigUseCase)

    @Test
    fun `a stored 2G policy returns 2G state`() {
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy2G

        val actualPolicy = useCase.getState()

        assertEquals(VerificationPolicyState.Policy2G, actualPolicy)
    }

    @Test
    fun `a stored 3G policy returns 3G state`() {
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy3G

        val actualPolicy = useCase.getState()

        assertEquals(VerificationPolicyState.Policy3G, actualPolicy)
    }

    @Test
    fun `no stored policy returns no state`() {
        every { persistenceManager.getVerificationPolicySelected() } returns null

        val actualPolicy = useCase.getState()

        assertEquals(VerificationPolicyState.None, actualPolicy)
    }

    @Test
    fun `storing the policy first time is only setting the correct policy`() {
        every { persistenceManager.isVerificationPolicySelectionSet() } returns false
        every { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy2G) } returns Unit

        useCase.store(VerificationPolicy.VerificationPolicy2G)

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy2G) }
        verify(exactly = 0) { persistenceManager.storeLastScanLockTimeSeconds(any()) }
    }

    @Test
    fun `storing the same policy again is not locking the scanner`() {
        every { persistenceManager.isVerificationPolicySelectionSet() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy2G
        every { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy2G) } returns Unit

        useCase.store(VerificationPolicy.VerificationPolicy2G)

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy2G) }
        verify(exactly = 0) { persistenceManager.storeLastScanLockTimeSeconds(any()) }
    }

    @Test
    fun `storing the policy second time onwards is setting the correct policy and storing the lock timestamp`() {
        every { persistenceManager.isVerificationPolicySelectionSet() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy3G
        every { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy2G) } returns Unit
        every { persistenceManager.storeLastScanLockTimeSeconds(1638316800) } returns Unit

        useCase.store(VerificationPolicy.VerificationPolicy2G)

        verify { persistenceManager.storeLastScanLockTimeSeconds(1638316800) }
        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy2G) }
    }

    @Test
    fun `switch state is locked if asked within five minutes`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds } returns 3600
        every { persistenceManager.getLastScanLockTimeSeconds() } returns 1638319800

        val actualSwitchState = useCase.getSwitchState()

        assertEquals(1638319800, (actualSwitchState as VerificationPolicySwitchState.Locked).lastScanLockTimeSeconds)
    }

    @Test
    fun `switch state is unlocked if asked after five minutes`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds } returns 3600
        every { persistenceManager.getLastScanLockTimeSeconds() } returns 1638313100

        val actualSwitchState = useCase.getSwitchState()

        assertEquals(VerificationPolicySwitchState.Unlocked, actualSwitchState)
    }
}
