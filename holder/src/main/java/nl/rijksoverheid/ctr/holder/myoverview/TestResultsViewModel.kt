package nl.rijksoverheid.ctr.holder.myoverview

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.usecase.TestResult
import nl.rijksoverheid.ctr.holder.usecase.TestResultUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val testResultUseCase: TestResultUseCase,
    private val persistenceManager: PersistenceManager
) : ViewModel() {

    val testResult: LiveData<Event<TestResult>> = MutableLiveData()
    val loading: LiveData<Event<Boolean>> = MutableLiveData()

    var verificationCode: String = savedStateHandle["verification_code"] ?: ""
        set(value) {
            field = value
            savedStateHandle["verification_code"] = value
            updateViewState()
        }

    var verificationRequired: Boolean = savedStateHandle["verification_required"] ?: false
        private set(value) {
            field = value
            savedStateHandle["verification_required"] = value
            updateViewState()
        }

    var testCode: String = savedStateHandle["test_code"] ?: ""
        set(value) {
            field = value
            savedStateHandle["test_code"] = value
            updateViewState()
        }

    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())

    val retrievedResult: TestResult.Complete?
        get() = (testResult.value?.peekContent() as? TestResult.Complete)

    private val currentViewState: ViewState
        get() = viewState.value!!

    init {
        updateViewState()
    }

    private fun updateViewState() {
        (viewState as MutableLiveData).value = currentViewState.copy(
            verificationRequired = verificationRequired,
            canRetrieveResult = (testCode.isNotEmpty() && !verificationRequired) || (verificationRequired && testCode.isNotEmpty() && verificationCode.isNotEmpty())
        )
    }

    fun getTestResult() {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val result = testResultUseCase.testResult(testCode, verificationCode)
                if (result == TestResult.VerificationRequired) {
                    verificationRequired = true
                }
                (testResult as MutableLiveData).value = Event(result)
            } finally {
                loading.value = Event(false)
            }
        }
    }

    fun saveTestResult() {
        retrievedResult?.let {
            persistenceManager.saveCredentials(it.credentials)
        }
    }

    data class ViewState(
        val verificationRequired: Boolean = false,
        val canRetrieveResult: Boolean = false
    )
}
