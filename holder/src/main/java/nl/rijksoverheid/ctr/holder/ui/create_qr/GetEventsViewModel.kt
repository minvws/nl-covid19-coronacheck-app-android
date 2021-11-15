package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMijnCnEventsUsecase
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

    abstract fun getEvents(
        jwt: String,
        originType: OriginType,
        withIncompleteVaccination: Boolean = false,
        mijnCN : Boolean = false
    )
}

class GetEventsViewModelImpl(
    private val eventUseCase: GetEventsUseCase,
    private val mijnCnEventsUsecase: GetMijnCnEventsUsecase
) : GetEventsViewModel() {

    override fun getEvents(
        jwt: String,
        originType: OriginType,
        withIncompleteVaccination: Boolean,
        mijnCN : Boolean
    ) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val events = if(!mijnCN) {
                    eventUseCase.getEvents(
                        jwt = jwt,
                        originType = originType,
                        withIncompleteVaccination = withIncompleteVaccination
                    )
                } else {
                    mijnCnEventsUsecase.getEvents(
                        jwt = jwt,
                        originType = originType
                    )
                }

                (eventsResult as MutableLiveData).value = Event(events)
            } finally {
                loading.value = Event(false)
            }
        }
    }
}