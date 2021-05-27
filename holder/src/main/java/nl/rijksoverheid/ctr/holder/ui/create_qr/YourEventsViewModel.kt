package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
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
    val savedEvents: LiveData<Event<Boolean>> = MutableLiveData()

    abstract fun saveRemoteTestResult(remoteTestResult: RemoteTestResult, rawResponse: ByteArray)
    abstract fun saveRemoteEvents(remoteEvents: Map<RemoteEvents, ByteArray>)
}

class YourEventsViewModelImpl(
    private val saveEventsUseCase: SaveEventsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
) : YourEventsViewModel() {

    override fun saveRemoteTestResult(remoteTestResult: RemoteTestResult, rawResponse: ByteArray) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                saveEventsUseCase.save(remoteTestResult, rawResponse)
                holderDatabaseSyncer.sync()
                (savedEvents as MutableLiveData).value = Event(true)
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun saveRemoteEvents(remoteEvents: Map<RemoteEvents, ByteArray>) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                saveEventsUseCase.save(remoteEvents)
                holderDatabaseSyncer.sync()
                (savedEvents as MutableLiveData).value = Event(true)
            } finally {
                loading.value = Event(false)
            }
        }
    }
}
