package nl.rijksoverheid.ctr.holder.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.usecase.TestResultUseCase
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.Result

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultsViewModel(private val testResultUseCase: TestResultUseCase) : ViewModel() {

    val testResultLiveData = MutableLiveData<Result<RemoteTestResult>>()

    fun getTestResult(uniqueCode: String, verificationCode: String) {
        testResultLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val testResult = testResultUseCase.testResult(
                    uniqueCode = uniqueCode,
                    verificationCode = verificationCode
                )
                withContext(Dispatchers.Main) {
                    testResultLiveData.value = Result.Success(testResult)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    testResultLiveData.value = Result.Failed(e)
                }
            }
        }
    }
}
