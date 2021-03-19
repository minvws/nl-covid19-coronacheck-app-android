/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class VerifiedQrResultState : Parcelable {
    @Parcelize
    data class Valid(val qrResult: VerifiedQr) : VerifiedQrResultState(), Parcelable

    @Parcelize
    object Invalid : VerifiedQrResultState(), Parcelable
}
