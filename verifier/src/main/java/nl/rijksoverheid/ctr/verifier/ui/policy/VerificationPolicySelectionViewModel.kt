package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase

abstract class VerificationPolicySelectionViewModel: ViewModel() {
    var radioButtonSelected: Int? = null

    abstract fun storeSelection(verificationPolicy: VerificationPolicy)
    abstract fun updateRadioButton(checkedId: Int)
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
) : VerificationPolicySelectionViewModel() {

    override fun storeSelection(verificationPolicy: VerificationPolicy) {
        verificationPolicyUseCase.store(verificationPolicy)
    }

    override fun updateRadioButton(checkedId: Int) {
        radioButtonSelected = checkedId
    }
}