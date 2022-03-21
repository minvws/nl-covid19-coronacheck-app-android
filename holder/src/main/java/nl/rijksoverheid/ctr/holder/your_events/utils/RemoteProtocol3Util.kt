/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import nl.rijksoverheid.ctr.holder.your_events.RemoteEventInformation
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol3

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
            it.events?.forEach { remoteEvent ->
                if (sameEventsGrouped.contains(remoteEvent)) {
                    sameEventsGrouped[remoteEvent]?.add(RemoteEventInformation(provider, holder, remoteEvent))
                } else {
                    sameEventsGrouped[remoteEvent] = mutableListOf(RemoteEventInformation(provider, holder, remoteEvent))
                }
            }
        }

        // Sort events descending by date
        return sameEventsGrouped.entries
            .sortedByDescending { it.key.getDate() }
            .associate { it.key to it.value }
    }
}