package nl.rijksoverheid.ctr.holder.persistence.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

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
    private val greenCardUtil: GreenCardUtil,
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
                if (syncWithRemote && events.isNotEmpty()) {
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
                            val result = syncRemoteGreenCardsUseCase.execute(
                                remoteGreenCards = remoteGreenCardsResult.remoteGreenCards
                            )

                            when (result) {
                                is SyncRemoteGreenCardsResult.Success -> {
                                    return@withContext DatabaseSyncerResult.Success
                                }
                                is SyncRemoteGreenCardsResult.Failed -> {
                                    return@withContext DatabaseSyncerResult.Failed.Error(result.errorResult)
                                }
                            }
                        }
                        is RemoteGreenCardsResult.Error -> {
                            val greenCards = holderDatabase.greenCardDao().getAll()

                            when (remoteGreenCardsResult.errorResult) {
                                is NetworkRequestResult.Failed.NetworkError -> {
                                    DatabaseSyncerResult.Failed.NetworkError(
                                        errorResult = remoteGreenCardsResult.errorResult,
                                        hasGreenCardsWithoutCredentials = greenCards
                                            .any { greenCardUtil.hasNoActiveCredentials(it) }
                                    )
                                }
                                is NetworkRequestResult.Failed.CoronaCheckHttpError -> {
                                    DatabaseSyncerResult.Failed.ServerError(
                                        errorResult = remoteGreenCardsResult.errorResult
                                    )
                                }
                                else -> {
                                    DatabaseSyncerResult.Failed.Error(
                                        errorResult = remoteGreenCardsResult.errorResult
                                    )
                                }
                            }
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
    sealed class Failed(open val errorResult: ErrorResult): DatabaseSyncerResult() {
        data class NetworkError(override val errorResult: ErrorResult, val hasGreenCardsWithoutCredentials: Boolean): Failed(errorResult)
        data class ServerError(override val errorResult: ErrorResult): Failed(errorResult)
        data class Error(override val errorResult: ErrorResult): Failed(errorResult)
    }
}
