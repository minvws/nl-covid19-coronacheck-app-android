package nl.rijksoverheid.ctr.verifier.models

import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ScannerState(open val verificationPolicyState: VerificationPolicyState) {

    data class Locked(val lastScanLockTimeSeconds: Long,
                      override val verificationPolicyState: VerificationPolicyState
    ): ScannerState(verificationPolicyState)

    data class Unlocked(override val verificationPolicyState: VerificationPolicyState
    ): ScannerState(verificationPolicyState)
}