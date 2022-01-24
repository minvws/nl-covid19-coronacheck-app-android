package nl.rijksoverheid.ctr.verifier.ui.policy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class VerificationPolicyUseCaseImplTest {

    private val persistenceManager: PersistenceManager = mockk()

    private val clock = Clock.fixed(Instant.parse("2021-12-01T00:00:00.00Z"), ZoneId.of("UTC"))
    private val useCase =
        VerificationPolicySelectionUseCaseImpl(persistenceManager, clock)

    @Test
    fun `storing the policy first time is only setting the correct policy`() {
        every { persistenceManager.isVerificationPolicySelectionSet() } returns false
        every { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) } returns Unit

        useCase.store(VerificationPolicy.VerificationPolicy1G)

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) }
        verify(exactly = 0) { persistenceManager.storeLastScanLockTimeSeconds(any()) }
    }

    @Test
    fun `storing the same policy again is not locking the scanner`() {
        every { persistenceManager.isVerificationPolicySelectionSet() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy1G
        every { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) } returns Unit

        useCase.store(VerificationPolicy.VerificationPolicy1G)

        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) }
        verify(exactly = 0) { persistenceManager.storeLastScanLockTimeSeconds(any()) }
    }

    @Test
    fun `storing the policy second time onwards is setting the correct policy and storing the lock timestamp`() {
        every { persistenceManager.isVerificationPolicySelectionSet() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy3G
        every { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) } returns Unit
        every { persistenceManager.storeLastScanLockTimeSeconds(1638316800) } returns Unit

        useCase.store(VerificationPolicy.VerificationPolicy1G)

        verify { persistenceManager.storeLastScanLockTimeSeconds(1638316800) }
        verify { persistenceManager.setVerificationPolicySelected(VerificationPolicy.VerificationPolicy1G) }
    }
}
