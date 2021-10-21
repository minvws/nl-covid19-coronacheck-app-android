package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VerificationResult(
    val status: Long,
    val details: VerificationResultDetails,
    val error: String
) : Parcelable
