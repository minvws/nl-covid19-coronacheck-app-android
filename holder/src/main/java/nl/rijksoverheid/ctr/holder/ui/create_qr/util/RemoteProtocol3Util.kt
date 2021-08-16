package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.create_qr.RemoteEventInformation
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3

interface RemoteProtocol3Util {
    fun groupEvents(remoteEvents: List<RemoteProtocol3>): Map<RemoteEvent, List<RemoteEventInformation>>
}

class RemoteProtocol3UtilImpl: RemoteProtocol3Util {

    /**
     * Group all events that have the same:
     * - the same date
     * AND
     * hpkcodes are not null and match
     * OR
     * manifacturer are not null and match
     * It's possible that your vaccination is known at both the GGD or RIVM
     * so this merges the two
     */
    override fun groupEvents(remoteEvents: List<RemoteProtocol3>): Map<RemoteEvent, List<RemoteEventInformation>> {
        val sameEventsGrouped = mutableMapOf<RemoteEvent, MutableList<RemoteEventInformation>>()

        remoteEvents.sortedBy { it.providerIdentifier }.forEach {
            val provider = it.providerIdentifier
            val holder = it.holder
            it.events?.sortedBy { date -> date.getDate() }?.forEach { remoteEvent ->
                if (sameEventsGrouped.contains(remoteEvent)) {
                    sameEventsGrouped[remoteEvent]?.add(RemoteEventInformation(provider, holder, remoteEvent))
                } else {
                    sameEventsGrouped[remoteEvent] = mutableListOf(RemoteEventInformation(provider, holder, remoteEvent))
                }
            }
        }

        return sameEventsGrouped
    }
}