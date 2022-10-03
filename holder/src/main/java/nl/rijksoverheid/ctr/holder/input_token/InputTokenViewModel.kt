/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.input_token

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.input_token.usecases.TestResult
import nl.rijksoverheid.ctr.holder.input_token.usecases.TestResultUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class InputTokenViewModel : ViewModel() {
    abstract fun updateViewState()
    abstract fun getTestResult(fromDeeplink: Boolean = false)
    abstract fun sendVerificationCode()

    open var verificationCode: String? = null
    open var verificationRequired: Boolean = false
    open var testCode: String = ""
    open var fromDeeplink: Boolean = false

    val testResult: LiveData<Event<TestResult>> = MutableLiveData()
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())
}

class InputTokenViewModelImpl(
    private val savedStateHandle: SavedStateHandle,
    private val testResultUseCase: TestResultUseCase
) : InputTokenViewModel() {

    override var verificationCode: String? = savedStateHandle["verification_code"]
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
    val fromDeeplink: Boolean = false
)
