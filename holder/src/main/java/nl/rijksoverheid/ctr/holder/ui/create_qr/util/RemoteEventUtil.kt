package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination

interface RemoteEventUtil {
    fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent>
}

class RemoteEventUtilImpl: RemoteEventUtil {

    /**
     * Only remove duplicate events for vaccination events
     */
    override fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent> {
        return if (remoteEvents.all { it is RemoteEventVaccination }) {
            return remoteEvents.filterIsInstance<RemoteEventVaccination>().distinct()
        } else {
            remoteEvents
        }
    }
}