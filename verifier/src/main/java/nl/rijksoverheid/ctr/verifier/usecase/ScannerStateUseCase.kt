package nl.rijksoverheid.ctr.verifier.usecase

import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyStateUseCase
import java.time.Clock
import java.time.Instant

interface ScannerStateUseCase {
    fun get(): ScannerState
}

class ScannerStateUseCaseImpl(
    private val clock: Clock,
    private val verificationPolicyStateUseCase: VerificationPolicyStateUseCase,
    private val verifierCachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager
): ScannerStateUseCase {

    override fun get(): ScannerState {
        val verificationPolicyState = verificationPolicyStateUseCase.get()

        val now = Instant.now(clock)
        val lockSeconds = verifierCachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong()

        val lastScanLockTimeSeconds = persistenceManager.getLastScanLockTimeSeconds()

        val policyChangeIsAllowed = Instant.ofEpochSecond(lastScanLockTimeSeconds).plusSeconds(lockSeconds).isBefore(now)

        return when {
            policyChangeIsAllowed -> ScannerState.Unlocked(verificationPolicyState)
            else -> ScannerState.Locked(
                lastScanLockTimeSeconds = lastScanLockTimeSeconds,
                verificationPolicyState = verificationPolicyState
            )
        }
    }
}