package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
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
abstract class HolderNameSelectionViewModel(
    holderDatabase: HolderDatabase,
    greenCardUtil: GreenCardUtil
) : FuzzyMatchingBaseViewModel(holderDatabase, greenCardUtil) {
    val itemsLiveData: LiveData<List<HolderNameSelectionItem>> = MutableLiveData()

    abstract fun onItemSelected(selectedName: String)
    abstract fun selectedName(): String?
    abstract fun storeSelection(onStored: () -> Unit)
    abstract fun nothingSelectedError()
}

class HolderNameSelectionViewModelImpl(
    private val matchedEventsUseCase: MatchedEventsUseCase,
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val selectionDataUtil: SelectionDataUtil,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
    private val holderDatabase: HolderDatabase,
    greenCardUtil: GreenCardUtil,
    private val matchingBlobIds: List<List<Int>>
) : HolderNameSelectionViewModel(holderDatabase, greenCardUtil) {

    init {
        updateItems()
    }

    override fun onItemSelected(selectedName: String) {
        updateItems(selectedName)
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

    override fun nothingSelectedError() {
        updateItems(nothingSelectedError = true)
    }

    private fun updateItems(
        selectedName: String? = null,
        nothingSelectedError: Boolean = false
    ) {
        viewModelScope.launch {
            val allEvents = holderDatabase.eventGroupDao().getAll()
            val fuzzyMatchedRemoteProtocols = matchingBlobIds.map { eventsCluster ->
                eventsCluster.mapNotNull { fuzzyMatchedEventId ->
                    allEvents.find { it.id == fuzzyMatchedEventId }
                }.mapNotNull(getRemoteProtocolFromEventGroupUseCase::get)
            }

            val holderNames = fuzzyMatchedRemoteProtocols.map { it.map { yourEventsFragmentUtil.getFullName(it.holder) } }
            val selectionItems = fuzzyMatchedRemoteProtocols.mapIndexed { index, remoteProtocols ->
                val holders = remoteProtocols.map { it.holder }.map { yourEventsFragmentUtil.getFullName(it) }
                val events = remoteProtocols.flatMap { it.events ?: emptyList() }.sortedByDescending { it.getDate() }
                val providerIdentifier = remoteProtocols.first().providerIdentifier
                // make sure we don't select a name present in other groups
                val otherHolderNames = holderNames.filterIndexed { i, _ -> i != index }.flatten()
                val name = holders.find { !otherHolderNames.contains(it) } ?: "mpourda"
                HolderNameSelectionItem.ListItem(
                    name = name,
                    events = selectionDataUtil.events(events),
                    detailData = selectionDataUtil.details(providerIdentifier, events),
                    isSelected = name == selectedName,
                    willBeRemoved = selectedName != null && name != selectedName,
                    nothingSelectedError = nothingSelectedError
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
