package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

@Parcelize
data class QrCodeFragmentData(
    val type: GreenCardType,
    val originType: OriginType,
    val credentials: List<ByteArray>,
    val shouldDisclose: Boolean, // If we should refresh this qr based on a time interval,
    val credentialExpirationTimeSeconds: Long
): Parcelable