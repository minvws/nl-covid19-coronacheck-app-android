package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.verifier.usecases.VerifierAllowsHolderUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierViewModel(
    private val verifierAllowsholderUseCase: VerifierAllowsHolderUseCase
) : ViewModel() {

    val holderAllowedLiveData = MutableLiveData<Result<Boolean>>()

    fun validateholder(holderQrContent: String) {
        viewModelScope.launch {
            try {
                val allowed = verifierAllowsholderUseCase.allow(
                    holderQrContent = holderQrContent
                )
                holderAllowedLiveData.postValue(Result.Success(allowed))
            } catch (e: Exception) {
                holderAllowedLiveData.postValue(Result.Failed(e))
            }
        }
    }
}
