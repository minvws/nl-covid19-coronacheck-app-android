/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import java.time.OffsetDateTime

data class SavedEvents(
    val provider: String,
    val events: List<SavedEvent>
) {

    data class SavedEvent(
        val type: OriginType,
        val date: OffsetDateTime
    )
}