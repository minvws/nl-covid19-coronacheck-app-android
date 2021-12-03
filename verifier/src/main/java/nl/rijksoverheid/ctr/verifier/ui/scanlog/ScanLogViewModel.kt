package nl.rijksoverheid.ctr.verifier.ui.scanlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem
import nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase.GetScanLogItemsUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class ScanLogViewModel : ViewModel() {
    open val scanLogItemsLiveData: LiveData<List<ScanLogItem>> = MutableLiveData()

    abstract fun getItems()
}

class ScanLogViewModelImpl(
    private val getScanLogItemsUseCase: GetScanLogItemsUseCase
): ScanLogViewModel() {

    override fun getItems() {
        viewModelScope.launch {
            (scanLogItemsLiveData as MutableLiveData).postValue(getScanLogItemsUseCase.getItems())
        }
    }
}

