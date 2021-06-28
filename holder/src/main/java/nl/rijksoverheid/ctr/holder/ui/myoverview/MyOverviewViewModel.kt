package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
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
    val myOverviewRefreshErrorEvent: LiveData<Event<MyOverviewError>> = MutableLiveData()

    abstract fun getSelectedType(): GreenCardType

    /**
     * Refresh all the items we need to display on the overview
     * @param selectType The type of green cards you want to show, null if refresh the current selected one
     * @param syncDatabase If you want to sync the database before showing the items
     */
    abstract fun refreshOverviewItems(selectType: GreenCardType = getSelectedType(), syncDatabase: Boolean = false)
}

class MyOverviewViewModelImpl(
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val persistenceManager: PersistenceManager,
    private val greenCardsUseCase: GreenCardsUseCase,
) : MyOverviewViewModel() {

    override fun getSelectedType(): GreenCardType {
        return (myOverviewItemsLiveData.value?.peekContent()?.selectedType
            ?: persistenceManager.getSelectedGreenCardType())
    }

    override fun refreshOverviewItems(selectType: GreenCardType, syncDatabase: Boolean) {
        // When the app is opened we need to remember the tab that was selected
        persistenceManager.setSelectedGreenCardType(selectType)

        viewModelScope.launch {
            if (syncDatabase) {
                holderDatabaseSyncer.sync(
                    syncWithRemote = false
                )
            }

            (myOverviewItemsLiveData as MutableLiveData).postValue(
                Event(
                    getMyOverviewItemsUseCase.get(
                        selectedType = selectType,
                        walletId = 1
                    )
                )
            )
        }
    }
}

sealed class MyOverviewError {
    object Inactive: MyOverviewError()
    object Refresh: MyOverviewError()

    companion object {
        fun get(expired: Boolean) = if (expired) {
            Inactive
        } else {
            Refresh
        }
    }
}
