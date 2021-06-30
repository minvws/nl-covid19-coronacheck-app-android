package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import java.time.Clock
import java.time.OffsetDateTime

interface RemoteEventRecoveryUtil {

    fun isExpired(remoteEventRecovery: RemoteEvent): Boolean
}

class RemoteEventRecoveryUtilImpl(private val clock: Clock): RemoteEventRecoveryUtil {

    override fun isExpired(remoteEventRecovery: RemoteEvent): Boolean {
        return OffsetDateTime.now(clock).minusDays(180) >= remoteEventRecovery.getDate()
    }
}