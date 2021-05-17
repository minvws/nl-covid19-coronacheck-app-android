package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.models.Wallet

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class MyOverviewViewModel(
) : ViewModel() {
    open val walletLiveData: LiveData<List<Wallet>> = MutableLiveData()

    abstract fun sync()
}

class MyOverviewViewModelImpl(
    holderDatabase: HolderDatabase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
) : MyOverviewViewModel() {

    override val walletLiveData = holderDatabase.walletDao().get()
        .map { wallets -> wallets.filter { wallet -> wallet.walletEntity.id == 1 } }
        .asLiveData()

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
