package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.verifier.models.VerifiedQr
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState

sealed class ScanResultValidData(open val verifiedQr: VerifiedQr) : Parcelable {
    @Parcelize
    data class Valid(override val verifiedQr: VerifiedQr) : ScanResultValidData(verifiedQr), Parcelable

    @Parcelize
    data class Demo(override val verifiedQr: VerifiedQr): ScanResultValidData(verifiedQr), Parcelable
}