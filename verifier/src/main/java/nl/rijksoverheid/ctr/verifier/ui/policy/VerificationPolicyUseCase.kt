package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import java.time.Clock
import java.time.Instant

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface VerificationPolicyUseCase {
    fun store(verificationPolicy: VerificationPolicy)
}

class VerificationPolicyUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val clock: Clock,
): VerificationPolicyUseCase {

    override fun store(verificationPolicy: VerificationPolicy) {
        val nowSeconds = Instant.now(clock).epochSecond

        // don't store a lock change the first time policy is set
        // or there is no change in the policy set
        if (persistenceManager.isVerificationPolicySelectionSet() && persistenceManager.getVerificationPolicySelected() != verificationPolicy) {
            persistenceManager.storeLastScanLockTimeSeconds(nowSeconds)
        }

        persistenceManager.setVerificationPolicySelected(verificationPolicy)
    }
}