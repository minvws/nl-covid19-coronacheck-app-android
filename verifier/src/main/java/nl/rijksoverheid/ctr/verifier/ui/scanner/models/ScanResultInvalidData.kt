package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.shared.models.VerificationResult

sealed class ScanResultInvalidData : Parcelable {
    @Parcelize
    data class Invalid(val verifiedQr: VerificationResult) : ScanResultInvalidData(), Parcelable

    @Parcelize
    data class Error(val error: String) :
        ScanResultInvalidData(), Parcelable
}
