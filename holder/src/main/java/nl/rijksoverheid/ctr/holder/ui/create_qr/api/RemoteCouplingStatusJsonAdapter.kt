package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCouplingStatus
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol

class RemoteCouplingStatusJsonAdapter {
    @FromJson
    fun fromJson(value: String?): RemoteCouplingStatus = when(value) {
        RemoteCouplingStatus.TYPE_EXPIRED -> RemoteCouplingStatus.Expired
        RemoteCouplingStatus.TYPE_BLOCKED -> RemoteCouplingStatus.Blocked
        RemoteCouplingStatus.TYPE_REJECTED -> RemoteCouplingStatus.Rejected
        RemoteCouplingStatus.TYPE_ACCEPTED -> RemoteCouplingStatus.Accepted
        else -> RemoteCouplingStatus.Rejected
    }

    @ToJson
    fun toJson(value: RemoteCouplingStatus): String = value.typeString
}