package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.*
import kotlinx.coroutines.launch
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

abstract class CommercialTestCodeViewModel : ViewModel() {
    abstract fun updateViewState()
    abstract fun getTestResult(fromDeeplink: Boolean = false)
    abstract fun sendVerificationCode()

    open var verificationCode: String = ""
    open var verificationRequired: Boolean = false
    open var testCode: String = ""
    open var fromDeeplink: Boolean = false

    val testResult: LiveData<Event<TestResult>> = MutableLiveData()
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())
}

open class CommercialTestCodeViewModelImpl(
    private val savedStateHandle: SavedStateHandle,
    private val testResultUseCase: TestResultUseCase
) : CommercialTestCodeViewModel() {

    override var verificationCode: String = savedStateHandle["verification_code"] ?: ""
        set(value) {
            field = value
            savedStateHandle["verification_code"] = value
            updateViewState()
        }

    override var verificationRequired: Boolean = savedStateHandle["verification_required"] ?: false
        set(value) {
            field = value
            savedStateHandle["verification_required"] = value
            updateViewState()
        }

    override var testCode: String = savedStateHandle["test_code"] ?: ""
        set(value) {
            field = value
            savedStateHandle["test_code"] = value
            updateViewState()
        }

    override var fromDeeplink: Boolean = savedStateHandle["from_deeplink"] ?: false
        set(value) {
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
            if (result is TestResult.Error) {
                (testResult as MutableLiveData).value = Event(result)
            }
        }
    }
}

data class ViewState(
    val verificationRequired: Boolean = false,
    val canRetrieveResult: Boolean = false,
    val fromDeeplink: Boolean = false
)
