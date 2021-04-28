package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ScanResultInvalidData : Parcelable {
    @Parcelize
    data class Invalid(val verifiedQr: VerifiedQr) : ScanResultInvalidData(), Parcelable

    @Parcelize
    data class Error(val error: String): ScanResultInvalidData(), Parcelable
}
