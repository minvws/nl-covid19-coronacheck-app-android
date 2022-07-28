/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.get_events.models.EventsResult
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetEventsUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetMijnCnEventsUsecase
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
        loginType: LoginType,
        jwt: String,
        originTypes: List<RemoteOriginType>,
        getPositiveTestWithVaccination: Boolean = false
    )

    abstract fun getMijnCnEvents(
        jwt: String,
        originType: RemoteOriginType,
        getPositiveTestWithVaccination: Boolean = false
    )
}

class GetEventsViewModelImpl(
    private val getEventsUseCase: GetEventsUseCase,
    private val mijnCnEventsUsecase: GetMijnCnEventsUsecase
) : GetEventsViewModel() {

    override fun getDigidEvents(
        loginType: LoginType,
        jwt: String,
        originTypes: List<RemoteOriginType>,
        getPositiveTestWithVaccination: Boolean
    ) {
        getEvents {
            getEventsUseCase.getEvents(
                loginType = loginType,
                jwt = jwt,
                originTypes = originTypes,
            )
        }
    }

    override fun getMijnCnEvents(
        jwt: String,
        originType: RemoteOriginType,
        getPositiveTestWithVaccination: Boolean
    ) {
        getEvents {
            mijnCnEventsUsecase.getEvents(
                jwt = jwt,
                originType = originType,
                withIncompleteVaccination = getPositiveTestWithVaccination
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