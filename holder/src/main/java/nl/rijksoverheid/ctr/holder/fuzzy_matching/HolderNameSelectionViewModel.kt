package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class HolderNameSelectionViewModel : ViewModel() {
    val itemsLiveData: LiveData<List<HolderNameSelectionItem>> = MutableLiveData()

    abstract fun onItemSelected(index: Int)
    abstract fun selectedName(): String?
}

class HolderNameSelectionViewModelImpl(
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val selectionDataUtil: SelectionDataUtil,
    private val holderDatabase: HolderDatabase,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil
) : HolderNameSelectionViewModel() {
    override fun onItemSelected(index: Int) {
        postItems(index - 1)
    }

    override fun selectedName(): String? {
        return itemsLiveData.value
            ?.filterIsInstance<HolderNameSelectionItem.ListItem>()
            ?.find { it.isSelected }
            ?.name
    }

    // TODO will be removed in next task and will be populated from a usecase
    init {
        postItems()
    }

    private fun postItems(selectedIndex: Int? = null) {
        viewModelScope.launch {
            val eventGroupEntities = holderDatabase.eventGroupDao().getAll()
            val remoteProtocols =
                eventGroupEntities.mapNotNull(getRemoteProtocolFromEventGroupUseCase::get)
            val holderEvents =
                mutableListOf<Triple<String, RemoteProtocol.Holder, List<RemoteEvent>>>()
            remoteProtocols.forEach {
                if (it.holder != null && it.events != null) {
                    holderEvents.add(Triple(it.providerIdentifier, it.holder, it.events))
                }
            }

            val items = holderEvents.mapIndexed() { index, (providerIdentifier, holder, events) ->
                HolderNameSelectionItem.ListItem(
                    name = yourEventsFragmentUtil.getFullName(holder),
                    events = selectionDataUtil.events(events),
                    detailData = selectionDataUtil.details(providerIdentifier, events),
                    isSelected = index == selectedIndex,
                    willBeRemoved = selectedIndex != null && index != selectedIndex
                )
            }.toTypedArray()

            (itemsLiveData as MutableLiveData).postValue(
                listOf(
                    HolderNameSelectionItem.HeaderItem,
                    *items,
                    HolderNameSelectionItem.FooterItem
                )
            )
        }
    }
}
