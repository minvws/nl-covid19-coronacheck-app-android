package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

@Parcelize
data class QrCodeFragmentData(
    val type: GreenCardType,
    val originType: OriginType,
    val credential: ByteArray,
    val shouldDisclose: Boolean, // If we should refresh this qr based on a time interval,
    val credentialExpirationTimeSeconds: Long
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QrCodeFragmentData

        if (!credential.contentEquals(other.credential)) return false
        if (shouldDisclose != other.shouldDisclose) return false
        if (credentialExpirationTimeSeconds != other.credentialExpirationTimeSeconds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credential.contentHashCode()
        result = 31 * result + shouldDisclose.hashCode()
        result = 31 * result + credentialExpirationTimeSeconds.hashCode()
        return result
    }
}