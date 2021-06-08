package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.shared.livedata.Event
import timber.log.Timber
import java.time.OffsetDateTime

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

    /**
     * Refresh all the items we need to display on the overview
     * @param selectType The type of green cards you want to show, null if refresh the current selected one
     * @param syncDatabase If you want to sync the database before showing the items
     */
    abstract fun refreshOverviewItems(selectType: GreenCardType? = null, syncDatabase: Boolean = false)
}

class MyOverviewViewModelImpl(
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
) : MyOverviewViewModel() {

    override fun getSelectedType(): GreenCardType {
        return (myOverviewItemsLiveData.value?.peekContent()?.selectedType
            ?: GreenCardType.Domestic)
    }

    override fun refreshOverviewItems(selectType: GreenCardType?, syncDatabase: Boolean) {
        viewModelScope.launch {
            if (syncDatabase) {
                holderDatabaseSyncer.sync(
                    syncWithRemote = false
                )
            }

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
}
