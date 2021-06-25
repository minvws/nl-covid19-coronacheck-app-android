package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteProtocol3(
    override val providerIdentifier: String,
    override val protocolVersion: String,
    override val status: Status,
    val holder: Holder?,
    val events: List<RemoteEvent>?): RemoteProtocol(providerIdentifier, protocolVersion, status), Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Holder(
        val infix: String?,
        val firstName: String?,
        val lastName: String?,
        val birthDate: String?
    ) : Parcelable

    override fun hasEvents(): Boolean {
        return events?.isNotEmpty() ?: false
    }
}
