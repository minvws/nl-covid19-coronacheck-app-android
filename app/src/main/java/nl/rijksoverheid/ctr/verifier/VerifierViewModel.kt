package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.models.Result

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierViewModel(
) : ViewModel() {

    val holderAllowedLiveData = MutableLiveData<Result<Boolean>>()

    fun validateholder(holderQrContent: String) {
        viewModelScope.launch {
            try {
                holderAllowedLiveData.postValue(Result.Success(true))
            } catch (e: Exception) {
                holderAllowedLiveData.postValue(Result.Failed(e))
            }
        }
    }
}
