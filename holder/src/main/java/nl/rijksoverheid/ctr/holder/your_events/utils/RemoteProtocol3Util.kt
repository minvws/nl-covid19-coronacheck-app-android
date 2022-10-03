/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import java.lang.StringBuilder
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.your_events.RemoteEventInformation

interface RemoteProtocol3Util {
    fun areGGDEvents(providerIdentifier: String): Boolean
    fun areRIVMEvents(providerIdentifier: String): Boolean
    fun getProviderIdentifier(remoteProtocol: RemoteProtocol): String
    fun groupEvents(remoteEvents: List<RemoteProtocol>): Map<RemoteEvent, List<RemoteEventInformation>>
}

class RemoteProtocol3UtilImpl : RemoteProtocol3Util {

    override fun getProviderIdentifier(remoteProtocol: RemoteProtocol): String {
        return if (!areGGDEvents(remoteProtocol.providerIdentifier) && !areRIVMEvents(remoteProtocol.providerIdentifier)) {
            val providerIdentifierBuilder = StringBuilder()
            providerIdentifierBuilder.append(remoteProtocol.providerIdentifier)
            providerIdentifierBuilder.append("_")
            remoteProtocol.events?.forEach { remoteEvent ->
                run {
                    providerIdentifierBuilder.append(remoteEvent.unique)
                }
            }
            providerIdentifierBuilder.toString()
        } else {
            remoteProtocol.providerIdentifier
        }
    }

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
    override fun groupEvents(remoteEvents: List<RemoteProtocol>): Map<RemoteEvent, List<RemoteEventInformation>> {
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

    override fun areGGDEvents(providerIdentifier: String): Boolean {
        return providerIdentifier.lowercase() == "ggd"
    }

    override fun areRIVMEvents(providerIdentifier: String): Boolean {
        return providerIdentifier.lowercase() == "rivm"
    }
}
