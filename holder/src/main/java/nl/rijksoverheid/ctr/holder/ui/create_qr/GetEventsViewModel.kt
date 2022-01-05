package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetDigidEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMijnCnEventsUsecase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetEventsUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class GetEventsViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val eventsResult: LiveData<Event<EventsResult>> = MutableLiveData()

    abstract fun getDigidEvents(
        jwt: String,
        originType: RemoteOriginType,
        withIncompleteVaccination: Boolean = false
    )

    abstract fun getMijnCnEvents(
        jwt: String,
        originType: RemoteOriginType,
        withIncompleteVaccination: Boolean = false
    )
}

class GetEventsViewModelImpl(
    private val getDigidEventsUseCase: GetDigidEventsUseCase,
    private val mijnCnEventsUsecase: GetMijnCnEventsUsecase
) : GetEventsViewModel() {

    override fun getDigidEvents(
        jwt: String,
        originType: RemoteOriginType,
        withIncompleteVaccination: Boolean
    ) {
        getEvents() {
            getDigidEventsUseCase.getEvents(
                jwt = jwt,
                originType = originType,
                withIncompleteVaccination = withIncompleteVaccination
            )
        }
    }

    override fun getMijnCnEvents(
        jwt: String,
        originType: RemoteOriginType,
        withIncompleteVaccination: Boolean
    ) {
        getEvents() {
            mijnCnEventsUsecase.getEvents(
                jwt = jwt,
                originType = originType,
                withIncompleteVaccination = withIncompleteVaccination
            )
        }
    }

    fun getEvents(function: suspend () -> EventsResult) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val events = function.invoke()
                (eventsResult as MutableLiveData).value = Event(events)
            } finally {
                loading.value = Event(false)
            }
        }
    }
}