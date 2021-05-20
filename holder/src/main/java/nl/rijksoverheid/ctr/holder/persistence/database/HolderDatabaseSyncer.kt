package nl.rijksoverheid.ctr.holder.persistence.database

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderDatabaseSyncer {
    suspend fun sync(syncWithRemote: Boolean = false): DatabaseSyncerResult
}

class HolderDatabaseSyncerImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val coronaCheckRepository: CoronaCheckRepository
) : HolderDatabaseSyncer {

    override suspend fun sync(syncWithRemote: Boolean): DatabaseSyncerResult {
        removeExpiredEventGroups()
        // TODO Remove expired green cards
        // TODO Remove expired credentials

        return if (syncWithRemote) {
            syncGreenCards()
        } else {
            DatabaseSyncerResult.Success
        }
    }

    /**
     * Check if we need to remove events from the database
     */
    private suspend fun removeExpiredEventGroups() {
        val events = holderDatabase.eventGroupDao().getAll()
        events.forEach {
            val expireDate =
                if (it.type == EventType.Vaccination) cachedAppConfigUseCase.getCachedAppConfigVaccinationEventValidity()
                    .toLong() else cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours()

            if (it.maxIssuedAt.plusHours(expireDate.toLong()) <= OffsetDateTime.now()) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }

    private suspend fun syncGreenCards(): DatabaseSyncerResult {
        return try {
            val remoteCredentials = coronaCheckRepository.getCredentials()
            Timber.v(
                "Remote credentials: $remoteCredentials"
            )
            DatabaseSyncerResult.Success
        } catch (e: HttpException) {
            DatabaseSyncerResult.ServerError(e.code())
        } catch (e: IOException) {
            DatabaseSyncerResult.NetworkError
        }
    }
}

sealed class DatabaseSyncerResult {
    object Success : DatabaseSyncerResult()
    data class ServerError(val httpCode: Int, val errorCode: Int? = null) : DatabaseSyncerResult()
    object NetworkError : DatabaseSyncerResult()
}
