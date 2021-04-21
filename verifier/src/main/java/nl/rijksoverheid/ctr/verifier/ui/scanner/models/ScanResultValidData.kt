package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState

sealed class ScanResultValidData : Parcelable {
    @Parcelize
    data class Valid(val valid: VerifiedQrResultState.Valid) : ScanResultValidData(), Parcelable

    @Parcelize
    data class Demo(val demo: VerifiedQrResultState.Demo): ScanResultValidData(), Parcelable
}