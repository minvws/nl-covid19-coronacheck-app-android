/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreen
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity

data class SavedEvents(
    val providerName: String,
    val eventGroupEntity: EventGroupEntity,
    val events: List<SavedEvent>
) {
    data class SavedEvent(
        val remoteEvent: RemoteEvent,
        val infoScreen: InfoScreen
    )
}