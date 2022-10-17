/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.saved_events.usecases.GetSavedEventsUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason
import nl.rijksoverheid.ctr.shared.livedata.Event

class SavedEventsViewModel(
    private val holderDatabase: HolderDatabase,
    private val getSavedEventsUseCase: GetSavedEventsUseCase
) : ViewModel() {

    val savedEventsLiveData: LiveData<Event<List<SavedEvents>>> = MutableLiveData()
    val removedSavedEventsLiveData: LiveData<Unit> = MutableLiveData()

    fun getSavedEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedEvents = getSavedEventsUseCase.getSavedEvents()
            (savedEventsLiveData as MutableLiveData).postValue(Event(savedEvents))
        }
    }

    fun removeSavedEvents(eventGroupEntity: EventGroupEntity) {
        viewModelScope.launch {
            holderDatabase.eventGroupDao().delete(eventGroupEntity)
            // the user deleted consciously a stored event and is aware of the currently stored events
            // so no point to communicate anymore which conflicted events were deleted
            holderDatabase.removedEventDao().deleteAll(RemovedEventReason.FuzzyMatched)
            (removedSavedEventsLiveData as MutableLiveData).postValue(Unit)
        }
    }
}
