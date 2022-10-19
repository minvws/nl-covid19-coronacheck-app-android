package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
abstract class HolderNameSelectionViewModel : ViewModel() {
    val itemsLiveData: LiveData<List<HolderNameSelectionItem>> = MutableLiveData()
    val canSkipLiveData: LiveData<Boolean> = MutableLiveData()

    abstract fun onItemSelected(selectedName: String)
    abstract fun selectedName(): String?
    abstract fun storeSelection(onStored: () -> Unit)
}

class HolderNameSelectionViewModelImpl(
    private val matchedEventsUseCase: MatchedEventsUseCase,
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val selectionDataUtil: SelectionDataUtil,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
    private val holderDatabase: HolderDatabase,
    private val greenCardUtil: GreenCardUtil,
    private val matchingBlobIds: List<List<Int>>
) : HolderNameSelectionViewModel() {

    init {
        updateItems()
        checkIfCanSkip()
    }

    private fun checkIfCanSkip() {
        viewModelScope.launch {
            val activeCredentialExists = holderDatabase.greenCardDao().getAll()
                .any { !greenCardUtil.hasNoActiveCredentials(it) }
            (canSkipLiveData as MutableLiveData).value = activeCredentialExists
        }
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

    private fun updateItems(
        selectedName: String? = null
    ) {
        viewModelScope.launch {
            val allEvents = holderDatabase.eventGroupDao().getAll()
            val fuzzyMatchedRemoteProtocols = matchingBlobIds.map { eventsCluster ->
                eventsCluster.mapNotNull { fuzzyMatchedEventId ->
                    allEvents.find { it.id == fuzzyMatchedEventId }
                }.mapNotNull(getRemoteProtocolFromEventGroupUseCase::get)
            }

            val selectionItems = fuzzyMatchedRemoteProtocols.map { remoteProtocols ->
                val holder = remoteProtocols.first().holder
                val events = remoteProtocols.flatMap { it.events ?: emptyList() }
                val providerIdentifier = remoteProtocols.first().providerIdentifier
                val name = yourEventsFragmentUtil.getFullName(holder)
                HolderNameSelectionItem.ListItem(
                    name = name,
                    events = selectionDataUtil.events(events),
                    detailData = selectionDataUtil.details(providerIdentifier, events),
                    isSelected = name == selectedName,
                    willBeRemoved = selectedName != null && name != selectedName
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
