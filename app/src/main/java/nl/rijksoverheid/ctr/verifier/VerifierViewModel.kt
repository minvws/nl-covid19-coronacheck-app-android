package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.verifier.usecases.VerifierAllowsCitizenUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierViewModel(
    private val verifierAllowsCitizenUseCase: VerifierAllowsCitizenUseCase
) : ViewModel() {

    val citizenAllowedLiveData = MutableLiveData<Result<Boolean>>()

    fun validateCitizen(citizenQrContent: String) {
        viewModelScope.launch {
            try {
                val allowed = verifierAllowsCitizenUseCase.allow(
                    citizenQrContent = citizenQrContent
                )
                citizenAllowedLiveData.postValue(Result.Success(allowed))
            } catch (e: Exception) {
                citizenAllowedLiveData.postValue(Result.Failed(e))
            }
        }
    }
}
