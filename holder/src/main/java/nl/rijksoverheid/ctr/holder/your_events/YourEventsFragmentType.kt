/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

sealed class YourEventsFragmentType : Parcelable {

    @Parcelize
    data class RemoteProtocol3Type(
        val remoteEvents: Map<RemoteProtocol, ByteArray>,
        val eventProviders: List<EventProvider> = emptyList()
    ) : YourEventsFragmentType()

    @Parcelize
    data class DCC(
        val remoteEvent: RemoteProtocol,
        val eventGroupJsonData: ByteArray,
        val originType: OriginType
    ) : YourEventsFragmentType(), Parcelable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DCC

            if (remoteEvent != other.remoteEvent) return false
            if (!eventGroupJsonData.contentEquals(other.eventGroupJsonData)) return false
            if (originType != other.originType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = remoteEvent.hashCode()
            result = 31 * result + eventGroupJsonData.contentHashCode()
            result = 31 * result + originType.hashCode()
            return result
        }

        fun getRemoteEvents(): Map<RemoteProtocol, ByteArray> =
            mapOf(remoteEvent to eventGroupJsonData)
    }
}
