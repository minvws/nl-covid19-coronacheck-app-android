/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.models.ConflictingEventResult
import nl.rijksoverheid.ctr.holder.your_events.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.holder.your_events.usecases.SaveEventsUseCaseImpl
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.persistence.database.usecases.DraftEventUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class YourEventsViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val yourEventsResult: LiveData<Event<DatabaseSyncerResult>> = MutableLiveData()
    val conflictingEventsResult: LiveData<Event<ConflictingEventResult>> = MutableLiveData()

    abstract fun saveRemoteProtocolEvents(
        flow: Flow,
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        removePreviousEvents: Boolean
    )

    abstract fun checkForConflictingEvents(remoteProtocols: Map<RemoteProtocol, ByteArray>)
}

data class RemoteEventInformation(
    val providerIdentifier: String,
    val holder: RemoteProtocol.Holder?,
    val remoteEvent: RemoteEvent
)

class YourEventsViewModelImpl(
    private val saveEventsUseCase: SaveEventsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val draftEventUseCase: DraftEventUseCase
) : YourEventsViewModel() {

    override fun checkForConflictingEvents(remoteProtocols: Map<RemoteProtocol, ByteArray>) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            draftEventUseCase.remove()
            try {
                val conflictingEvents =
                    saveEventsUseCase.remoteProtocols3AreConflicting(remoteProtocols)

                (conflictingEventsResult as MutableLiveData).postValue(Event(conflictingEvents))
            } catch (e: Exception) {
                (yourEventsResult as MutableLiveData).value = Event(
                    DatabaseSyncerResult.Failed.Error(AppErrorResult(HolderStep.StoringEvents, e))
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun saveRemoteProtocolEvents(
        flow: Flow,
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        removePreviousEvents: Boolean
    ) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the events in the database
                val result = saveEventsUseCase.saveRemoteProtocols3(
                    remoteProtocols = remoteProtocols,
                    removePreviousEvents = removePreviousEvents,
                    flow = flow
                )

                when (result) {
                    is SaveEventsUseCaseImpl.SaveEventResult.Success -> {
                        // Send all events to database and create green cards, origins and credentials
                        val databaseSyncerResult = holderDatabaseSyncer.sync(
                            flow = flow,
                            newEvents = remoteProtocols.keys.flatMap { it.events ?: listOf() }
                        )

                        (yourEventsResult as MutableLiveData).value = Event(
                            databaseSyncerResult
                        )
                    }
                    is SaveEventsUseCaseImpl.SaveEventResult.Failed -> {
                        (yourEventsResult as MutableLiveData).value =
                            Event(DatabaseSyncerResult.Failed.Error(result.errorResult))
                    }
                }
            } catch (e: Exception) {
                (yourEventsResult as MutableLiveData).value = Event(
                    DatabaseSyncerResult.Failed.Error(AppErrorResult(HolderStep.StoringEvents, e))
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }
}
