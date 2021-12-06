package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy

abstract class VerificationPolicySelectionViewModel: ViewModel() {
    val liveData: LiveData<VerificationPolicyFlow> = MutableLiveData()

    abstract fun storeSelection(verificationPolicy: VerificationPolicy)
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
    private val isScanQRFlow: Boolean,
) : VerificationPolicySelectionViewModel() {

    init {
        val policyState = verificationPolicyUseCase.getState()

        (liveData as MutableLiveData).postValue(
            if (isScanQRFlow) {
                VerificationPolicyFlow.ScanQR(policyState)
            } else {
                VerificationPolicyFlow.Settings(policyState)
            }
        )
    }

    override fun storeSelection(verificationPolicy: VerificationPolicy) {
        verificationPolicyUseCase.store(verificationPolicy)
    }
}