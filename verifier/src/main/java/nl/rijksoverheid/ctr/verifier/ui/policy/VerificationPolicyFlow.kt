package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.verifier.models.ScannerState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class VerificationPolicyFlow(val state: ScannerState): Parcelable {
    @Parcelize
    class FirstTimeUse(val scannerState: ScannerState) : VerificationPolicyFlow(scannerState)
    @Parcelize
    class Info(val scannerState: ScannerState) : VerificationPolicyFlow(scannerState)
}
