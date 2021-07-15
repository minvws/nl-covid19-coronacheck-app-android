package nl.rijksoverheid.ctr.holder.ui.create_qr.models

sealed class RemoteCouplingStatus(val typeString: String) {

    companion object {
        const val TYPE_ACCEPTED = "accepted"
        const val TYPE_REJECTED = "rejected"
        const val TYPE_BLOCKED = "blocked"
        const val TYPE_EXPIRED = "expired"
    }

    object Accepted: RemoteCouplingStatus(TYPE_ACCEPTED)
    object Rejected: RemoteCouplingStatus(TYPE_REJECTED)
    object Blocked: RemoteCouplingStatus(TYPE_BLOCKED)
    object Expired: RemoteCouplingStatus(TYPE_EXPIRED)
}