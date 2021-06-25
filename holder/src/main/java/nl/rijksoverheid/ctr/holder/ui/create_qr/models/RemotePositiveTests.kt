package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
class RemotePositiveTests(
    override val providerIdentifier: String,
    override val protocolVersion: String,
    override val status: Status,
    val holder: Holder? = null,
    val events: List<RemoteEvent>? = null
) : RemoteProtocol3<RemoteEvent>(providerIdentifier, protocolVersion, status, holder, events), Parcelable {

    override fun hasEvents(): Boolean {
        return events?.isNotEmpty() ?: false
    }
}