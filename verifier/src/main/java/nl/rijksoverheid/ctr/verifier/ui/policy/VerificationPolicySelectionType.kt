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
sealed class VerificationPolicySelectionType(val state: ScannerState): Parcelable {
    @Parcelize
    class FirstTimeUse(val scannerState: ScannerState) : VerificationPolicySelectionType(scannerState)
    @Parcelize
    class Default(val scannerState: ScannerState) : VerificationPolicySelectionType(scannerState)
}
