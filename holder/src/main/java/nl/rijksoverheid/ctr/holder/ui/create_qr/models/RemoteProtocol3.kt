package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

abstract class RemoteProtocol3<E: RemoteEvent>(
    providerIdentifier: String,
    protocolVersion: String,
    status: Status,
    holder: Holder?,
    events: List<E>?) : RemoteProtocol(providerIdentifier, protocolVersion, status) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Holder(
        val infix: String?,
        val firstName: String?,
        val lastName: String?,
        val birthDate: String?
    ) : Parcelable
}
