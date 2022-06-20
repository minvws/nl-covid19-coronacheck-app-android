package nl.rijksoverheid.ctr.holder.qrcodes.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import java.time.OffsetDateTime

@Parcelize
data class QrCodeFragmentData(
    val type: GreenCardType,
    val originType: OriginType,
    val credentialsWithExpirationTime: List<Pair<ByteArray, OffsetDateTime>>,
    val shouldDisclose: ShouldDisclose,
): Parcelable {

    sealed class ShouldDisclose: Parcelable {

        @Parcelize
        object DoNotDisclose: ShouldDisclose(), Parcelable

        @Parcelize
        data class Disclose(val greenCardId: Int, val disclosurePolicy: GreenCardDisclosurePolicy): ShouldDisclose(), Parcelable
    }
}