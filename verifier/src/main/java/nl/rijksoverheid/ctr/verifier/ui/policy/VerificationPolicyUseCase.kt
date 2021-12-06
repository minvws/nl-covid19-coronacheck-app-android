package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface VerificationPolicyUseCase {
    fun get(): VerificationPolicyState
    fun store(verificationPolicy: VerificationPolicy)
    fun getSwitchState(): VerificationPolicySwitchState
    fun getRemainingSecondsLocked(): Long
}

class VerificationPolicyUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val clock: Clock,
    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
): VerificationPolicyUseCase {

    override fun get(): VerificationPolicyState {
        return when (persistenceManager.getVerificationPolicySelected()) {
            VerificationPolicy.VerificationPolicy2G -> VerificationPolicyState.Policy2G
            VerificationPolicy.VerificationPolicy3G -> VerificationPolicyState.Policy3G
            else -> VerificationPolicyState.None
        }
    }

    override fun store(verificationPolicy: VerificationPolicy) {
        val nowSeconds = Instant.now(clock).epochSecond

        // don't store a lock change the first time policy is set
        // or there is no change in the policy set
        if (persistenceManager.isVerificationPolicySelectionSet() && persistenceManager.getVerificationPolicySelected() != verificationPolicy) {
            persistenceManager.storeLastScanLockTimeSeconds(nowSeconds)
        }

        persistenceManager.setVerificationPolicySelected(verificationPolicy)
    }

    override fun getSwitchState(): VerificationPolicySwitchState {
        val now = Instant.now(clock)
        val lockSeconds = cachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong()
        
        val lastScanLockTimeSeconds = persistenceManager.getLastScanLockTimeSeconds()
        
        val policyChangeIsAllowed = Instant.ofEpochSecond(lastScanLockTimeSeconds).plusSeconds(lockSeconds).isBefore(now)

        return when {
            policyChangeIsAllowed -> VerificationPolicySwitchState.Unlocked
            else -> VerificationPolicySwitchState.Locked(lastScanLockTimeSeconds)
        }
    }

    override fun getRemainingSecondsLocked(): Long {
        val now = OffsetDateTime.now(clock).toEpochSecond()
        val lockSeconds = cachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong()

        val lastScanLockTimeSeconds = persistenceManager.getLastScanLockTimeSeconds()

        val remainingSeconds = lockSeconds + lastScanLockTimeSeconds - now

        return remainingSeconds
    }
}
