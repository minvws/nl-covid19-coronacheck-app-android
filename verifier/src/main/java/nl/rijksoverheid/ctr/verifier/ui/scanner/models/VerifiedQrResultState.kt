/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.shared.models.VerificationResult

sealed class VerifiedQrResultState : Parcelable {
    @Parcelize
    data class Valid(val verifiedQr: VerificationResult) : VerifiedQrResultState(), Parcelable

    @Parcelize
    data class InvalidInNL(val verifiedQr: VerificationResult) : VerifiedQrResultState(), Parcelable

    @Parcelize
    data class UnknownQR(val verifiedQr: VerificationResult) : VerifiedQrResultState(), Parcelable

    @Parcelize
    data class Error(val error: String) : VerifiedQrResultState(), Parcelable

    @Parcelize
    data class Demo(val verifiedQr: VerificationResult) : VerifiedQrResultState(), Parcelable
}
