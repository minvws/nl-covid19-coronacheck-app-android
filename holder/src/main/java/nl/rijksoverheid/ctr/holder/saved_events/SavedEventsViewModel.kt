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
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.EventGroupEntityUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.shared.livedata.Event
import org.json.JSONObject
import java.time.OffsetDateTime

class SavedEventsViewModel(
    private val holderDatabase: HolderDatabase,
    private val remoteEventUtil: RemoteEventUtil,
    private val eventGroupEntityUtil: EventGroupEntityUtil,
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase
): ViewModel() {

    val savedEventsLiveData: LiveData<Event<List<SavedEvents>>> = MutableLiveData()
    val removedSavedEventsLiveData: LiveData<Unit> = MutableLiveData()

    fun getSavedEvents() {
        viewModelScope.launch {
            val eventGroups = holderDatabase.eventGroupDao().getAll().asReversed()

            val savedEvents = eventGroups.map { eventGroupEntity ->
                val isDccEvent = remoteEventUtil.isDccEvent(
                    providerIdentifier = eventGroupEntity.providerIdentifier
                )
                val remoteProtocol = if (isDccEvent) {
                    val credential = JSONObject(eventGroupEntity.jsonData.decodeToString()).getString("credential")
                    getEventsFromPaperProofQrUseCase.get(credential)
                } else {
                    remoteEventUtil.getRemoteProtocol3FromNonDcc(
                        eventGroupEntity = eventGroupEntity
                    )
                }

                if (remoteProtocol == null) {
                    null
                } else {
                    SavedEvents(
                        eventGroupEntity = eventGroupEntity,
                        remoteProtocol3 = remoteProtocol,
                        providerIdentifier = eventGroupEntity.providerIdentifier,
                        providerName = eventGroupEntityUtil.getProviderName(
                            providerIdentifier = eventGroupEntity.providerIdentifier
                        ),
                        isDccEvent = isDccEvent
                    )
                }

            }.filterNotNull()

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