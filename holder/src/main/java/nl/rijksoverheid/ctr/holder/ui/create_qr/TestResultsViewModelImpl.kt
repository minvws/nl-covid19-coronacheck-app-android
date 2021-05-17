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

abstract class TestResultsViewModel : ViewModel() {
    abstract fun updateViewState()
    abstract fun getTestResult(fromDeeplink: Boolean = false)
    abstract fun sendVerificationCode()
    abstract fun saveTestResult()
    abstract fun getRetrievedResult(): TestResult.NegativeTestResult?

    val testResult: LiveData<Event<TestResult>> = MutableLiveData()
    val signedTestResult = MutableLiveData<Event<SignedTestResult>>()
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())
}

open class TestResultsViewModelImpl(
    private val savedStateHandle: SavedStateHandle,
    private val testResultUseCase: TestResultUseCase,
    private val persistenceManager: PersistenceManager,
    private val secretKeyUseCase: SecretKeyUseCase
) : TestResultsViewModel() {

    override fun getRetrievedResult(): TestResult.NegativeTestResult? {
        return (testResult.value?.peekContent() as? TestResult.NegativeTestResult)
    }

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


    private val currentViewState: ViewState
        get() = viewState.value!!

    init {
        updateViewState()
    }

    override fun updateViewState() {
        (viewState as MutableLiveData).value = currentViewState.copy(
            verificationRequired = verificationRequired,
            canRetrieveResult = (testCode.isNotEmpty() && !verificationRequired) || (verificationRequired && testCode.isNotEmpty() && verificationCode.isNotEmpty()),
            fromDeeplink = fromDeeplink
        )
    }

    override fun getTestResult(fromDeeplink: Boolean) {
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

    override fun sendVerificationCode() {
        viewModelScope.launch {
            val result = testResultUseCase.testResult(testCode, "")

            // Only notify the UI of errors, since this is just about resending a sms verification on the backend
            if (result is TestResult.NetworkError || result is TestResult.ServerError) {
                (testResult as MutableLiveData).value = Event(result)
            }
        }
    }

    override fun saveTestResult() {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                secretKeyUseCase.persist()
                getRetrievedResult()?.let {
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
