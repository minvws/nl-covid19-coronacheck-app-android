/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTime
import nl.rijksoverheid.ctr.holder.get_events.models.*
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.holder.saved_events.usecases.GetSavedEventsUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.EventGroupEntityUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.shared.livedata.Event
import org.json.JSONObject

class SavedEventsViewModel(
    private val holderDatabase: HolderDatabase,
    private val getSavedEventsUseCase: GetSavedEventsUseCase
): ViewModel() {

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
            (removedSavedEventsLiveData as MutableLiveData).postValue(Unit)
        }
    }
}