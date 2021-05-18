package nl.rijksoverheid.ctr.holder.persistence.database

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import java.time.LocalDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderDatabaseSyncer {
    suspend fun sync()
}

class HolderDatabaseSyncerImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : HolderDatabaseSyncer {

    override suspend fun sync() {
        removeExpiredEventGroups()
    }

    /**
     * Check if we need to remove events from the database
     */
    private suspend fun removeExpiredEventGroups() {
        val events = holderDatabase.eventGroupDao().getAll()
        events.forEach {
            if (it.maxIssuedAt.atStartOfDay().plusHours(
                    cachedAppConfigUseCase.getCachedAppConfigVaccinationEventValidity().toLong()
                ) <= LocalDateTime.now()
            ) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }
}
