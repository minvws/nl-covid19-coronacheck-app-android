package nl.rijksoverheid.ctr.holder.util

import nl.rijksoverheid.ctr.shared.models.Event
import org.threeten.bp.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class EventUtil {

    fun timeValid(event: Event): Boolean {
        return event.validFrom <= OffsetDateTime.now()
            .toEpochSecond() && event.validTo >= OffsetDateTime.now().toEpochSecond()
    }
}
