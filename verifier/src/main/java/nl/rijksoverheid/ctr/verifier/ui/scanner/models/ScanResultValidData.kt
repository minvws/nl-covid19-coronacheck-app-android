package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.appconfig.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.shared.models.VerificationResult

sealed class ScanResultValidData(
    open val verifiedQr: VerificationResult,
    open val externalReturnAppData: ExternalReturnAppData?
) : Parcelable {
    @Parcelize
    data class Valid(
        override val verifiedQr: VerificationResult,
        override val externalReturnAppData: ExternalReturnAppData?
    ) : ScanResultValidData(verifiedQr, externalReturnAppData), Parcelable

    @Parcelize
    data class Demo(
        override val verifiedQr: VerificationResult,
        override val externalReturnAppData: ExternalReturnAppData?
    ) : ScanResultValidData(verifiedQr, externalReturnAppData), Parcelable
}
