package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

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
    val conflictingEventsResult: LiveData<Event<Boolean>> = MutableLiveData()

    abstract fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray)
    abstract fun saveRemoteProtocol3Events(
        remoteProtocols3: Map<RemoteProtocol3, ByteArray>,
        originType: OriginType,
        removePreviousEvents: Boolean
    )

    abstract fun checkForConflictingEvents(remoteProtocols3: Map<RemoteProtocol3, ByteArray>)
    abstract fun combineSameVaccinationEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent>
    abstract fun combineSameEventsFromDifferentProviders(remoteEvents: List<RemoteProtocol3>): Map<RemoteEvent, List<RemoteEventInformation>>
}

data class RemoteEventInformation(val providerIdentifier: String, val holder: RemoteProtocol3.Holder?, val remoteEvent: RemoteEvent)

class YourEventsViewModelImpl(
    private val saveEventsUseCase: SaveEventsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
) : YourEventsViewModel() {

    override fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the event in the database
                saveEventsUseCase.saveNegativeTest2(negativeTest2, rawResponse)

                // Send all events to database and create green cards, origins and credentials
                val databaseSyncerResult = holderDatabaseSyncer.sync(
                    expectedOriginType = OriginType.Test
                )

                (yourEventsResult as MutableLiveData).value = Event(
                    databaseSyncerResult
                )
            } catch (e: Exception) {
                (yourEventsResult as MutableLiveData).value = Event(
                    DatabaseSyncerResult.ServerError(999)
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun checkForConflictingEvents(remoteProtocols3: Map<RemoteProtocol3, ByteArray>) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val conflictingEvents =
                    saveEventsUseCase.remoteProtocols3AreConflicting(remoteProtocols3)

                (conflictingEventsResult as MutableLiveData).postValue(Event(conflictingEvents))
            } catch (e: Exception) {
                (yourEventsResult as MutableLiveData).value = Event(
                    DatabaseSyncerResult.ServerError(999)
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun saveRemoteProtocol3Events(
        remoteProtocols3: Map<RemoteProtocol3, ByteArray>,
        originType: OriginType,
        removePreviousEvents: Boolean
    ) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the events in the database
                saveEventsUseCase.saveRemoteProtocols3(
                    remoteProtocols3 = remoteProtocols3,
                    originType = originType,
                    removePreviousEvents = removePreviousEvents
                )

                // Send all events to database and create green cards, origins and credentials
                val databaseSyncerResult = holderDatabaseSyncer.sync(
                    expectedOriginType = originType
                )

                (yourEventsResult as MutableLiveData).value = Event(
                    databaseSyncerResult
                )

            } catch (e: Exception) {
                (yourEventsResult as MutableLiveData).value = Event(
                    DatabaseSyncerResult.ServerError(999)
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun combineSameVaccinationEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent> {
        // we combine only vaccination events
        if (remoteEvents.any { it !is RemoteEventVaccination }) {
            return remoteEvents
        }
        return remoteEvents.toSet().toList()
    }

    override fun combineSameEventsFromDifferentProviders(remoteEvents: List<RemoteProtocol3>): Map<RemoteEvent, List<RemoteEventInformation>> {
        val sameEventsGrouped = mutableMapOf<RemoteEvent, MutableList<RemoteEventInformation>>()

        remoteEvents.sortedBy { it.providerIdentifier }.forEach {
            val provider = it.providerIdentifier
            val holder = it.holder
            it.events?.sortedBy { it.getDate() }?.forEach { remoteEvent ->
                if (sameEventsGrouped.contains(remoteEvent)) {
                    sameEventsGrouped[remoteEvent]?.add(RemoteEventInformation(provider, holder, remoteEvent))
                } else {
                    sameEventsGrouped[remoteEvent] = mutableListOf(RemoteEventInformation(provider, holder, remoteEvent))
                }
            }
        }

        return sameEventsGrouped
    }
}
