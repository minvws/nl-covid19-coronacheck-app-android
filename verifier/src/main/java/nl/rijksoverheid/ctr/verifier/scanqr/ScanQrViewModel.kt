package nl.rijksoverheid.ctr.verifier.scanqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.livedata.SingleLiveEvent
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.verifier.usecases.TestResultValidUseCase
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrViewModel(
    private val testResultValidUseCase: TestResultValidUseCase
) : ViewModel() {

    val qrValidLiveData = SingleLiveEvent<Result<Boolean>>()

    fun validate(currentDate: OffsetDateTime, qrContent: String) {
        qrValidLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val isValid = testResultValidUseCase.valid(
                    currentDate = currentDate,
                    qrContent = qrContent
                )
                withContext(Dispatchers.Main) {
                    qrValidLiveData.value = Result.Success(isValid)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    qrValidLiveData.value = Result.Failed(e)
                }
            }
        }
    }
}
