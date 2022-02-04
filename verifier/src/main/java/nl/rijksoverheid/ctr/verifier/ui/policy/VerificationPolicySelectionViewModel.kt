package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy

abstract class VerificationPolicySelectionViewModel: ViewModel() {
    var radioButtonSelected: Int? = null
    val scannerUsedRecentlyLiveData: LiveData<Event<Boolean>> = MutableLiveData()
    val storedVerificationPolicySelection: LiveData<Event<Unit>> = MutableLiveData()

    abstract fun storeSelection(verificationPolicy: VerificationPolicy)
    abstract fun updateRadioButton(checkedId: Int)
    abstract fun didScanRecently()
}

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerificationPolicySelectionViewModelImpl(
    private val verificationPolicySelectionUseCase: VerificationPolicySelectionUseCase,
    private val scannerUsedRecentlyUseCase: ScannerUsedRecentlyUseCase,
) : VerificationPolicySelectionViewModel() {

    override fun storeSelection(verificationPolicy: VerificationPolicy) {
        viewModelScope.launch {
            verificationPolicySelectionUseCase.store(verificationPolicy)
            (storedVerificationPolicySelection as MutableLiveData).postValue(Event(Unit))
        }
    }

    override fun updateRadioButton(checkedId: Int) {
        radioButtonSelected = checkedId
    }

    override fun didScanRecently() {
        viewModelScope.launch {
            (scannerUsedRecentlyLiveData as MutableLiveData).postValue(
                Event(
                    scannerUsedRecentlyUseCase.get()
                )
            )
        }
    }
}