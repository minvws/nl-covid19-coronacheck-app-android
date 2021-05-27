package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QrCodeFragmentData(
    val credential: ByteArray
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QrCodeFragmentData

        if (!credential.contentEquals(other.credential)) return false

        return true
    }

    override fun hashCode(): Int {
        return credential.contentHashCode()
    }
}