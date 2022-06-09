/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

interface UpdateEventExpirationUseCase {
    suspend fun update(blobExpireDates: List<RemoteGreenCards.BlobExpiry>)
}

class UpdateEventExpirationUseCaseImpl(
    private val holderDatabase: HolderDatabase
): UpdateEventExpirationUseCase {

    override suspend fun update(blobExpireDates: List<RemoteGreenCards.BlobExpiry>) {
        blobExpireDates.forEach {
            holderDatabase.eventGroupDao().updateExpiryDate(
                eventGroupId = it.id,
                expiryDate = it.expiry
            )
        }
    }
}