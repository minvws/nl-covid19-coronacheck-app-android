package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SignedTestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResultUseCase
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
    private val persistenceManager: PersistenceManager,
    private val secretKeyUseCase: SecretKeyUseCase
) : ViewModel() {

    val testResult: LiveData<Event<TestResult>> = MutableLiveData()
    val signedTestResult: LiveData<Event<SignedTestResult>> = MutableLiveData()
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

    var fromDeeplink: Boolean = savedStateHandle["from_deeplink"] ?: false
        private set(value) {
            field = value
            savedStateHandle["from_deeplink"] = value
            updateViewState()
        }

    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())

    val retrievedResult: TestResult.NegativeTestResult?
        get() = (testResult.value?.peekContent() as? TestResult.NegativeTestResult)

    private val currentViewState: ViewState
        get() = viewState.value!!

    init {
        updateViewState()
    }

    private fun updateViewState() {
        (viewState as MutableLiveData).value = currentViewState.copy(
            verificationRequired = verificationRequired,
            canRetrieveResult = (testCode.isNotEmpty() && !verificationRequired) || (verificationRequired && testCode.isNotEmpty() && verificationCode.isNotEmpty()),
            fromDeeplink = fromDeeplink
        )
    }

    fun getTestResult(fromDeeplink: Boolean = false) {
        this.fromDeeplink = fromDeeplink
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

    fun sendVerificationCode() {
        viewModelScope.launch {
            val result = testResultUseCase.testResult(testCode, "")

            // Only notify the UI of errors, since this is just about resending a sms verification on the backend
            if (result is TestResult.NetworkError || result is TestResult.ServerError) {
                (testResult as MutableLiveData).value = Event(result)
            }
        }
    }

    fun saveTestResult() {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Fixes 62688 - trying to sign result without persisted key
                secretKeyUseCase.persist()
                retrievedResult?.let {
                    val result = testResultUseCase.signTestResult(
                        signedResponseWithTestResult = it.signedResponseWithTestResult
                    )
                    if (result is SignedTestResult.Complete) {
                        persistenceManager.saveCredentials(result.credentials)
                    }
                    (signedTestResult as MutableLiveData).value = Event(result)
                }
            } finally {
                loading.value = Event(false)
            }
        }
    }
}

data class ViewState(
    val verificationRequired: Boolean = false,
    val canRetrieveResult: Boolean = false,
    val fromDeeplink: Boolean = false
)
