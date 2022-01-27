package nl.rijksoverheid.ctr.verifier.usecase

import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.policy.ConfigVerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionStateUseCase
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionUseCase
import java.time.Clock
import java.time.Instant

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScannerStateUseCase {
    fun get(): ScannerState
}

class ScannerStateUseCaseImpl(
    private val clock: Clock,
    private val verificationPolicySelectionStateUseCase: VerificationPolicySelectionStateUseCase,
    private val verifierCachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager,
    private val featureFlagUseCase: FeatureFlagUseCase,
) : ScannerStateUseCase {

    override fun get(): ScannerState {
        val verificationPolicyState = verificationPolicySelectionStateUseCase.get()

        val now = Instant.now(clock)
        val lockSeconds =
            verifierCachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong()

        val lastScanLockTimeSeconds = persistenceManager.getLastScanLockTimeSeconds()

        val policyChangeIsAllowed =
            !featureFlagUseCase.isVerificationPolicySelectionEnabled() ||
            Instant.ofEpochSecond(
                lastScanLockTimeSeconds
            ).plusSeconds(lockSeconds).isBefore(now)

        return when {
            policyChangeIsAllowed -> ScannerState.Unlocked(verificationPolicyState)
            else -> ScannerState.Locked(
                lastScanLockTimeSeconds = lastScanLockTimeSeconds,
                verificationPolicySelectionState = verificationPolicyState
            )
        }
    }
}