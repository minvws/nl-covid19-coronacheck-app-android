package nl.rijksoverheid.ctr.holder.qrcodes.models

import android.os.Parcelable
import java.time.OffsetDateTime
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

@Parcelize
data class QrCodeFragmentData(
    val type: GreenCardType,
    val originType: OriginType,
    val credentialsWithExpirationTime: List<Pair<ByteArray, OffsetDateTime>>,
    val shouldDisclose: ShouldDisclose
) : Parcelable {

    sealed class ShouldDisclose : Parcelable {

        @Parcelize
        object DoNotDisclose : ShouldDisclose(), Parcelable

        @Parcelize
        data class Disclose(val greenCardId: Int) : ShouldDisclose(), Parcelable
    }
}
