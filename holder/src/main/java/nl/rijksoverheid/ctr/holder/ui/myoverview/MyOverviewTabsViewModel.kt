package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class MyOverviewTabsViewModel: ViewModel() {
    open val showAddCertificateButtonEvent: LiveData<Boolean> = MutableLiveData()
}

class MyOverviewTabsViewModelImpl(
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase,
): MyOverviewTabsViewModel() {
    init {
        viewModelScope.launch {
            val greenCards = getMyOverviewItemsUseCase.getGreenCards()
            (showAddCertificateButtonEvent as MutableLiveData).postValue(greenCards.isEmpty())
        }
    }
}
