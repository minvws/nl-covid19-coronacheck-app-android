package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.usecases.TestResultValidUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrViewModel(
    private val testResultValidUseCase: TestResultValidUseCase,
    private val persistenceManager: PersistenceManager
) : ViewModel() {

    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val validatedQrLiveData = MutableLiveData<Event<VerifiedQrResultState>>()

    fun validate(qrContent: String) {
        loadingLiveData.value = Event(true)
        viewModelScope.launch {
            try {
                val result = testResultValidUseCase.valid(
                    qrContent = qrContent
                )
                when (result) {
                    is TestResultValidUseCase.TestResultValidResult.Valid -> {
                        validatedQrLiveData.value =
                            Event(VerifiedQrResultState.Valid(result.verifiedQr))
                    }
                    is TestResultValidUseCase.TestResultValidResult.Invalid -> {
                        validatedQrLiveData.value = Event(VerifiedQrResultState.Invalid)
                    }
                }
            } finally {
                loadingLiveData.value = Event(false)
            }
        }
    }

    fun scanInstructionsSeen(): Boolean {
        val seen = persistenceManager.getScanInstructionsSeen()
        if (!seen) {
            persistenceManager.setScanInstructionsSeen()
        }
        return seen
    }
}
