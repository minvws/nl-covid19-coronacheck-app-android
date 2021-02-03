package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.verifier.usecases.DecryptHolderQrUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MainViewModel(private val decryptHolderQrUseCase: DecryptHolderQrUseCase) : ViewModel() {

    val holderAllowedLiveData = MutableLiveData<nl.rijksoverheid.ctr.shared.models.Result<Boolean>>()

    fun validateholder(holderQrContent: String) {
        viewModelScope.launch {
            try {
                val timestamp = decryptHolderQrUseCase.decrypt(holderQrContent)
                holderAllowedLiveData.postValue(nl.rijksoverheid.ctr.shared.models.Result.Success(true))
            } catch (e: Exception) {
                holderAllowedLiveData.postValue(nl.rijksoverheid.ctr.shared.models.Result.Failed(e))
            }
        }
    }
}
