package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QrCodeFragmentData(
    val credential: ByteArray,
    val shouldDisclose: Boolean // If we should refresh this qr based on a time interval
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QrCodeFragmentData

        if (!credential.contentEquals(other.credential)) return false
        if (shouldDisclose != other.shouldDisclose) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credential.contentHashCode()
        result = 31 * result + shouldDisclose.hashCode()
        return result
    }
}