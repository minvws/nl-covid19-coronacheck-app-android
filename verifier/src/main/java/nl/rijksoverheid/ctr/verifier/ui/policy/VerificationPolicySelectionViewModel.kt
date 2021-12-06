package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy

sealed class VerificationPolicyFlow(val state: VerificationPolicyState) {
    class ScanQR(policyState: VerificationPolicyState) : VerificationPolicyFlow(policyState)
    class Settings(policyState: VerificationPolicyState) : VerificationPolicyFlow(policyState)
}

sealed class VerificationPolicyState {
    object None : VerificationPolicyState()
    object Policy2G : VerificationPolicyState()
    object Policy3G : VerificationPolicyState()
}

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
        val policy = when (verificationPolicyUseCase.get()) {
            VerificationPolicy.VerificationPolicy2G -> VerificationPolicyState.Policy2G
            VerificationPolicy.VerificationPolicy3G -> VerificationPolicyState.Policy3G
            else -> VerificationPolicyState.None
        }

        (liveData as MutableLiveData).postValue(
            if (isScanQRFlow) {
                VerificationPolicyFlow.ScanQR(policy)
            } else {
                VerificationPolicyFlow.Settings(policy)
            }
        )
    }

    override fun storeSelection(verificationPolicy: VerificationPolicy) {
        verificationPolicyUseCase.store(verificationPolicy)
    }
}