package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
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

    abstract fun saveNegativeTest2(remoteTestResult: RemoteTestResult2, rawResponse: ByteArray)
    abstract fun saveRemoteProtocol3Events(remoteProtocols3: Map<RemoteProtocol3, ByteArray>, originType: OriginType)
}

class YourEventsViewModelImpl(
    private val saveEventsUseCase: SaveEventsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
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

    override fun saveRemoteProtocol3Events(remoteProtocols3: Map<RemoteProtocol3, ByteArray>, originType: OriginType) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the events in the database
                saveEventsUseCase.saveRemoteProtocols3(
                    remoteProtocols3 = remoteProtocols3,
                    originType = originType)

                // Send all events to database and create green cards, origins and credentials
                val databaseSyncerResult = holderDatabaseSyncer.sync(
                    expectedOriginType = originType
                )

                (yourEventsResult as MutableLiveData).value = Event(
                    databaseSyncerResult
                )
            } catch(e: Exception) {
                (yourEventsResult as MutableLiveData).value = Event(
                    DatabaseSyncerResult.ServerError(999)
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }
}
