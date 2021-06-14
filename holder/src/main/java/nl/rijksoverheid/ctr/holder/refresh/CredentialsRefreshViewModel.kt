/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.refresh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase

abstract class CredentialsRefreshViewModel: ViewModel() {
    abstract fun refresh()
}

class CredentialsRefreshViewModelImpl(
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val greenCardsUseCase: GreenCardsUseCase,
): CredentialsRefreshViewModel() {
    override fun refresh() {
        viewModelScope.launch {
            val expiringCardOriginType = greenCardsUseCase.expiringCardOriginType()
            val needsRefresh = expiringCardOriginType != null
            if (needsRefresh) {
                holderDatabaseSyncer.sync(expiringCardOriginType, true)
            }
        }
    }
}
