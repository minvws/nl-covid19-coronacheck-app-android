package nl.rijksoverheid.ctr.holder.persistence.database

import androidx.room.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.persistence.WorkerManagerWrapper
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderDatabaseSyncer {

    /**
     * Synchronized the database. Does cleanup in the database based on expiration dates and can resync with remote
     * @param expectedOriginType If not null checks if the remote credentials contain this origin. Will return [DatabaseSyncerResult.MissingOrigin] if it's not present.
     * @param syncWithRemote If true and the data call to resync succeeds, clear all green cards in the database and re-add them
     */
    suspend fun sync(expectedOriginType: OriginType? = null, syncWithRemote: Boolean = true): DatabaseSyncerResult
}

class HolderDatabaseSyncerImpl(
    private val holderDatabase: HolderDatabase,
    private val workerManagerWrapper: WorkerManagerWrapper,
    private val greenCardUtil: GreenCardUtil,
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase,
    private val getRemoteGreenCardsUseCase: GetRemoteGreenCardsUseCase,
    private val syncRemoteGreenCardsUseCase: SyncRemoteGreenCardsUseCase
) : HolderDatabaseSyncer {

    private val mutex = Mutex()

    override suspend fun sync(
        expectedOriginType: OriginType?,
        syncWithRemote: Boolean
    ): DatabaseSyncerResult {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val events = holderDatabase.eventGroupDao().getAll()

                // Sync with remote
                if (syncWithRemote) {
                    val remoteGreenCardsResult = getRemoteGreenCardsUseCase.get(
                        events = events
                    )

                    when (remoteGreenCardsResult) {
                        is RemoteGreenCardsResult.Success -> {
                            val remoteGreenCards = remoteGreenCardsResult.remoteGreenCards

                            // If we expect the remote green cards to have a certain origin
                            if (expectedOriginType != null && !remoteGreenCards.getAllOrigins()
                                    .contains(expectedOriginType)
                            ) {
                                return@withContext DatabaseSyncerResult.MissingOrigin
                            }

                            // Insert green cards in database
                            try {
                                syncRemoteGreenCardsUseCase.execute(
                                    remoteGreenCards = remoteGreenCardsResult.remoteGreenCards
                                )
                            } catch (exception: Exception) {
                                // creating new credentials failed but previous cards and credentials not deleted
                            }

                            // Schedule refreshing of green cards in background
//                        workerManagerWrapper.scheduleNextCredentialsRefreshIfAny()

                            DatabaseSyncerResult.Success
                        }
                        is RemoteGreenCardsResult.Error.ServerError -> {
                            DatabaseSyncerResult.ServerError(remoteGreenCardsResult.httpCode)
                        }
                        is RemoteGreenCardsResult.Error.NetworkError -> {
                            val greenCards = holderDatabase.greenCardDao().getAll()
                            DatabaseSyncerResult.NetworkError(
                                hasGreenCardsWithoutCredentials = greenCards
                                    .any { greenCardUtil.hasNoActiveCredentials(it) }
                            )
                        }
                    }
                } else {
                    DatabaseSyncerResult.Success
                }
            }
        }
    }
}

sealed class DatabaseSyncerResult {
    object Loading : DatabaseSyncerResult()
    object Success : DatabaseSyncerResult()
    object MissingOrigin : DatabaseSyncerResult()
    data class ServerError(val httpCode: Int) : DatabaseSyncerResult()
    data class NetworkError(val hasGreenCardsWithoutCredentials: Boolean) : DatabaseSyncerResult()
}
