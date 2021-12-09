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
    val recentScanLogsLiveData: LiveData<Event<Boolean>> = MutableLiveData()

    abstract fun storeSelection(verificationPolicy: VerificationPolicy)
    abstract fun updateRadioButton(checkedId: Int)
    abstract fun checkForRecentScanLogs()
}

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerificationPolicySelectionViewModelImpl(
    private val verificationPolicyUseCase: VerificationPolicyUseCase,
    private val getRecentScanLogsUseCase: GetRecentScanLogsUseCase,
) : VerificationPolicySelectionViewModel() {

    override fun storeSelection(verificationPolicy: VerificationPolicy) {
        verificationPolicyUseCase.store(verificationPolicy)
    }

    override fun updateRadioButton(checkedId: Int) {
        radioButtonSelected = checkedId
    }

    override fun checkForRecentScanLogs() {
        viewModelScope.launch {
            (recentScanLogsLiveData as MutableLiveData).postValue(
                Event(
                    getRecentScanLogsUseCase.get()
                )
            )
        }
    }
}