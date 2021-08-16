package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination

interface RemoteEventUtil {
    fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent>
}

class RemoteEventUtilImpl: RemoteEventUtil {

    override fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent> {
        return remoteEvents.filterIsInstance<RemoteEventVaccination>().distinct()
    }
}