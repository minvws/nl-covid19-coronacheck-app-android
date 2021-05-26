package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class MyOverviewViewModel : ViewModel() {
    open val myOverviewItemsLiveData: LiveData<Event<MyOverviewItems>> = MutableLiveData()

    abstract fun getSelectedType(): GreenCardType
    abstract fun refreshOverviewItems(selectType: GreenCardType? = null)
    abstract fun sync()
}

class MyOverviewViewModelImpl(
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
) : MyOverviewViewModel() {

    override fun getSelectedType(): GreenCardType {
        return (myOverviewItemsLiveData.value?.peekContent()?.selectedType
            ?: GreenCardType.Domestic)
    }

    /**
     * Refresh all the items we need to display on the overview
     * @param selectType The type of green cards you want to show, null if refresh the current selected one
     */
    override fun refreshOverviewItems(selectType: GreenCardType?) {
        viewModelScope.launch {
            (myOverviewItemsLiveData as MutableLiveData).postValue(
                Event(
                    getMyOverviewItemsUseCase.get(
                        selectedType = selectType ?: getSelectedType(),
                        walletId = 1
                    )
                )
            )
        }
    }

    /**
     * Sync the database
     * This will do the correctly insert, update and delete calls
     * On each update, [walletLiveData] is automatically called with a new Wallet with updated entities
     */
    override fun sync() {
        viewModelScope.launch {
            holderDatabaseSyncer.sync()
        }
    }

}
