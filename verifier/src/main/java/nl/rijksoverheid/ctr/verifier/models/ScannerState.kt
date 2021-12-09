package nl.rijksoverheid.ctr.verifier.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ScannerState(open val verificationPolicyState: VerificationPolicyState): Parcelable {

    @Parcelize
    data class Locked(val lastScanLockTimeSeconds: Long,
                      override val verificationPolicyState: VerificationPolicyState
    ): ScannerState(verificationPolicyState)

    @Parcelize
    data class Unlocked(override val verificationPolicyState: VerificationPolicyState
    ): ScannerState(verificationPolicyState)
}