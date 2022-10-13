package nl.rijksoverheid.ctr.persistence.database

import java.time.OffsetDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.usecases.PersistBlockedEventsUseCase
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.workers.WorkerManagerUtil
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason
import nl.rijksoverheid.ctr.persistence.database.usecases.GetRemoteGreenCardsUseCase
import nl.rijksoverheid.ctr.persistence.database.usecases.RemoteGreenCardsResult
import nl.rijksoverheid.ctr.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.persistence.database.usecases.SyncRemoteGreenCardsResult
import nl.rijksoverheid.ctr.persistence.database.usecases.SyncRemoteGreenCardsUseCase
import nl.rijksoverheid.ctr.persistence.database.usecases.UpdateEventExpirationUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow
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
     * @param previousSyncResult The previous result outputted by this [sync] if known
     */
    suspend fun sync(
        flow: Flow = HolderFlow.Startup,
        syncWithRemote: Boolean = true,
        previousSyncResult: DatabaseSyncerResult? = null,
        newEvents: List<RemoteEvent> = listOf()
    ): DatabaseSyncerResult
}

class HolderDatabaseSyncerImpl(
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val holderDatabase: HolderDatabase,
    private val greenCardUtil: GreenCardUtil,
    private val workerManagerUtil: WorkerManagerUtil,
    private val getRemoteGreenCardsUseCase: GetRemoteGreenCardsUseCase,
    private val syncRemoteGreenCardsUseCase: SyncRemoteGreenCardsUseCase,
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase,
    private val updateEventExpirationUseCase: UpdateEventExpirationUseCase,
    private val persistBlockedEventsUseCase: PersistBlockedEventsUseCase
) : HolderDatabaseSyncer {

    private val mutex = Mutex()

    override suspend fun sync(
        flow: Flow,
        syncWithRemote: Boolean,
        previousSyncResult: DatabaseSyncerResult?,
        newEvents: List<RemoteEvent>
    ): DatabaseSyncerResult {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val events = holderDatabase.eventGroupDao().getAll()

                if (syncWithRemote) {
                    if (events.isEmpty()) {
                        // Remote does not handle empty events, so we decide that empty events == no green cards
                        holderDatabase.greenCardDao().deleteAll()
                        return@withContext DatabaseSyncerResult.Success(listOf())
                    }

                    // Generate a new secret key
                    val secretKey = mobileCoreWrapper.generateHolderSk()

                    // Sync with remote
                    val remoteGreenCardsResult = getRemoteGreenCardsUseCase.get(
                        events = events,
                        secretKey = secretKey,
                        flow = flow
                    )

                    when (remoteGreenCardsResult) {
                        is RemoteGreenCardsResult.Success -> {
                            // Update event expire dates
                            updateEventExpirationUseCase.update(
                                blobExpireDates = remoteGreenCardsResult.remoteGreenCards.blobExpireDates ?: listOf()
                            )

                            // Persist blocked events for communication to the user on the dashboard
                            persistBlockedEventsUseCase.persist(
                                newEvents = newEvents,
                                removedEvents = remoteGreenCardsResult.blockedEvents,
                                reason = RemovedEventReason.Blocked
                            )

                            val remoteGreenCards = remoteGreenCardsResult.remoteGreenCards

                            // Insert green cards in database
                            val result = syncRemoteGreenCardsUseCase.execute(
                                remoteGreenCards = remoteGreenCards,
                                secretKey = secretKey
                            )

                            // Clean up expired events in the database
                            removeExpiredEventsUseCase.execute(
                                events = holderDatabase.eventGroupDao().getAll()
                            )

                            when (result) {
                                is SyncRemoteGreenCardsResult.Success -> {
                                    workerManagerUtil.scheduleRefreshCredentialsJob()
                                    return@withContext DatabaseSyncerResult.Success(
                                        hints = remoteGreenCards.hints ?: listOf(),
                                        blockedEvents = remoteGreenCardsResult.blockedEvents
                                    )
                                }
                                is SyncRemoteGreenCardsResult.Failed -> {
                                    return@withContext DatabaseSyncerResult.Failed.Error(result.errorResult)
                                }
                            }
                        }
                        is RemoteGreenCardsResult.FuzzyMatchingError -> {
                            DatabaseSyncerResult.FuzzyMatchingError(
                                matchingBlobIds = remoteGreenCardsResult.matchingBlobIds
                            )
                        }
                        is RemoteGreenCardsResult.Error -> {
                            val greenCards = holderDatabase.greenCardDao().getAll()

                            when (remoteGreenCardsResult.errorResult) {
                                is NetworkRequestResult.Failed.ClientNetworkError, is NetworkRequestResult.Failed.ServerNetworkError -> {
                                    DatabaseSyncerResult.Failed.NetworkError(
                                        errorResult = remoteGreenCardsResult.errorResult,
                                        hasGreenCardsWithoutCredentials = greenCards
                                            .any { greenCardUtil.hasNoActiveCredentials(it) }
                                    )
                                }
                                is NetworkRequestResult.Failed.CoronaCheckHttpError -> {
                                    if (previousSyncResult == null) {
                                        DatabaseSyncerResult.Failed.ServerError.FirstTime(
                                            errorResult = remoteGreenCardsResult.errorResult
                                        )
                                    } else {
                                        DatabaseSyncerResult.Failed.ServerError.MultipleTimes(
                                            errorResult = remoteGreenCardsResult.errorResult
                                        )
                                    }
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
                    previousSyncResult ?: DatabaseSyncerResult.Success(listOf())
                }
            }
        }
    }
}

sealed class DatabaseSyncerResult {
    data class Success(
        val hints: List<String> = listOf(),
        val blockedEvents: List<RemoteEvent> = listOf()
    ) : DatabaseSyncerResult()

    data class FuzzyMatchingError(
        val matchingBlobIds: List<List<Int>>
    ) : DatabaseSyncerResult()

    sealed class Failed(open val errorResult: ErrorResult, open val failedAt: OffsetDateTime) :
        DatabaseSyncerResult() {
        data class NetworkError(
            override val errorResult: ErrorResult,
            val hasGreenCardsWithoutCredentials: Boolean
        ) : Failed(errorResult, OffsetDateTime.now())

        sealed class ServerError(override val errorResult: ErrorResult) :
            Failed(errorResult, OffsetDateTime.now()) {
            data class FirstTime(override val errorResult: ErrorResult) : ServerError(errorResult)
            data class MultipleTimes(override val errorResult: ErrorResult) :
                ServerError(errorResult)
        }

        data class Error(override val errorResult: ErrorResult) :
            Failed(errorResult, OffsetDateTime.now())
    }
}
