package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class EventViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val eventResult: LiveData<Event<EventResult>> = MutableLiveData()

    abstract fun getRetrievedResult(): EventResult.Success?
    abstract fun getEvents(digidToken: String)
}

class EventViewModelImpl(private val eventUseCase: EventUseCase) : EventViewModel() {
    override fun getRetrievedResult(): EventResult.Success? {
        return eventResult.value?.peekContent() as? EventResult.Success
    }

    override fun getEvents(digidToken: String) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            (eventResult as MutableLiveData).value =
                Event(eventUseCase.getVaccinationEvents(digidToken))
            loading.value = Event(false)
        }
    }
}
