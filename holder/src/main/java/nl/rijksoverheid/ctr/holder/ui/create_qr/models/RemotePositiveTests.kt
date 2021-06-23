package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RemotePositiveTests(
    providerIdentifier: String,
    protocolVersion: String,
    status: Status,
    holder: Holder? = null,
    val events: List<RemoteEvent>? = null
) : RemoteProtocol3<RemoteEvent>(providerIdentifier, protocolVersion, status, holder, events) {

    override fun hasEvents(): Boolean {
        return events?.isNotEmpty() ?: false
    }
}