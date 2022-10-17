package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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
    abstract fun storeSelection(onStored: () -> Unit)
}

class HolderNameSelectionViewModelImpl(
    private val matchedEventsUseCase: MatchedEventsUseCase,
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val selectionDataUtil: SelectionDataUtil,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
    private val holderDatabase: HolderDatabase,
    private val matchingBlobIds: List<List<Int>>
) : HolderNameSelectionViewModel() {

    init {
        updateItems()
    }

    override fun onItemSelected(index: Int) {
        updateItems(index - 1)
    }

    override fun selectedName(): String? {
        return itemsLiveData.value
            ?.filterIsInstance<HolderNameSelectionItem.ListItem>()
            ?.find { it.isSelected }
            ?.name
    }

    override fun storeSelection(onStored: () -> Unit) {
        val items = itemsLiveData.value?.filterIsInstance<HolderNameSelectionItem.ListItem>() ?: return
        val itemSelected = items.find { it.isSelected }
        if (itemSelected != null) {
            viewModelScope.launch {
                matchedEventsUseCase.selected(items.indexOf(itemSelected), matchingBlobIds)
                onStored()
            }
        }
    }

    private fun updateItems(
        selectedIndex: Int? = null
    ) {
        viewModelScope.launch {
            val allEvents = holderDatabase.eventGroupDao().getAll()
            val fuzzyMatchedRemoteProtocols = matchingBlobIds.map { eventsCluster ->
                eventsCluster.mapNotNull { fuzzyMatchedEventId ->
                    allEvents.find { it.id == fuzzyMatchedEventId }
                }.mapNotNull(getRemoteProtocolFromEventGroupUseCase::get)
            }

            val selectionItems = fuzzyMatchedRemoteProtocols.mapIndexed { index, remoteProtocols ->
                val holder = remoteProtocols.first().holder
                val events = remoteProtocols.flatMap { it.events ?: emptyList() }
                val providerIdentifier = remoteProtocols.first().providerIdentifier
                HolderNameSelectionItem.ListItem(
                    name = yourEventsFragmentUtil.getFullName(holder),
                    events = selectionDataUtil.events(events),
                    detailData = selectionDataUtil.details(providerIdentifier, events),
                    isSelected = index == selectedIndex,
                    willBeRemoved = selectedIndex != null && index != selectedIndex
                )
            }.toTypedArray()

            (itemsLiveData as MutableLiveData).value = listOf(
                HolderNameSelectionItem.HeaderItem,
                *selectionItems,
                HolderNameSelectionItem.FooterItem
            )
        }
    }
}
