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

sealed class ValidatedQrResultState : Parcelable {
    @Parcelize
    data class Valid(val qrResult: DecryptedQr) : ValidatedQrResultState(), Parcelable

    @Parcelize
    object Invalid : ValidatedQrResultState(), Parcelable
}