package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.shared.VerificationResult

sealed class ScanResultValidData(open val verifiedQr: VerificationResult) : Parcelable {
    @Parcelize
    data class Valid(override val verifiedQr: VerificationResult) : ScanResultValidData(verifiedQr), Parcelable

    @Parcelize
    data class Demo(override val verifiedQr: VerificationResult): ScanResultValidData(verifiedQr), Parcelable
}
