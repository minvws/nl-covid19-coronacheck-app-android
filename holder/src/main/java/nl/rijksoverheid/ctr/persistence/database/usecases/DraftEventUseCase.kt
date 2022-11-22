package nl.rijksoverheid.ctr.persistence.database.usecases

import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface DraftEventUseCase {
    suspend fun remove()
    suspend fun finalise()
}

class DraftEventUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : DraftEventUseCase {

    private val eventGroupDao = holderDatabase.eventGroupDao()

    override suspend fun remove() {
        eventGroupDao.deleteDraftEvents()
    }

    override suspend fun finalise() {
        eventGroupDao.updateDraft(false)
    }
}
