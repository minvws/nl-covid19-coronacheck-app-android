package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SaveEventsUseCaseImpl
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.AppErrorResult

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
                when (val result = saveEventsUseCase.saveNegativeTest2(negativeTest2, rawResponse)) {
                    is SaveEventsUseCaseImpl.SaveEventResult.Success -> {
                        // Send all events to database and create green cards, origins and credentials
                        val databaseSyncerResult = holderDatabaseSyncer.sync(
                            expectedOriginType = OriginType.Test
                        )

                        (yourEventsResult as MutableLiveData).value = Event(
                            databaseSyncerResult
                        )
                    }
                    is SaveEventsUseCaseImpl.SaveEventResult.Failed -> {
                        (yourEventsResult as MutableLiveData).value = Event(DatabaseSyncerResult.Failed.Error(result.errorResult))
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

    override fun checkForConflictingEvents(remoteProtocols3: Map<RemoteProtocol3, ByteArray>) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val conflictingEvents =
                    saveEventsUseCase.remoteProtocols3AreConflicting(remoteProtocols3)

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

    override fun saveRemoteProtocol3Events(
        remoteProtocols3: Map<RemoteProtocol3, ByteArray>,
        originType: OriginType,
        removePreviousEvents: Boolean
    ) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the events in the database
                val result = saveEventsUseCase.saveRemoteProtocols3(
                    remoteProtocols3 = remoteProtocols3,
                    originType = originType,
                    removePreviousEvents = removePreviousEvents
                )

                when (result) {
                    is SaveEventsUseCaseImpl.SaveEventResult.Success -> {
                        // Send all events to database and create green cards, origins and credentials
                        val databaseSyncerResult = holderDatabaseSyncer.sync(
                            expectedOriginType = originType
                        )

                        (yourEventsResult as MutableLiveData).value = Event(
                            databaseSyncerResult
                        )
                    }
                    is SaveEventsUseCaseImpl.SaveEventResult.Failed -> {
                        (yourEventsResult as MutableLiveData).value = Event(DatabaseSyncerResult.Failed.Error(result.errorResult))
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
