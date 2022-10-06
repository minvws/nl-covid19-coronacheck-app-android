package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
}

class HolderNameSelectionViewModelImpl : HolderNameSelectionViewModel() {
    override fun onItemSelected(index: Int) {
        (itemsLiveData as MutableLiveData).postValue(
            listOf(
                HolderNameSelectionItem.HeaderItem,
                *getListItems(index - 1),
                HolderNameSelectionItem.FooterItem
            )
        )
    }

    // TODO will be removed in next task and will be populated from a usecase
    init {
        val item = HolderNameSelectionItem.ListItem(
            name = "van Geer, Caroline Johanna Helena",
            events = "3 vaccinaties, 1 testuitslag, 1 Vaccinatiebeoordeling")
        (itemsLiveData as MutableLiveData).postValue(
            listOf(
                HolderNameSelectionItem.HeaderItem,
                *Array(2) { item },
                HolderNameSelectionItem.FooterItem
            )
        )
    }
    private fun getListItems(selectedIndex: Int): Array<HolderNameSelectionItem.ListItem> {
        val listItems = arrayOf(0, 1, 2)
        return listItems.mapIndexed { index, i ->
            HolderNameSelectionItem.ListItem(
                name = "van Geer, Caroline Johanna Helena",
                events = "3 vaccinaties en 1 testuitslag",
                isSelected = index == selectedIndex,
                willBeRemoved = index != selectedIndex
            )
        }.toTypedArray()
    }
}
