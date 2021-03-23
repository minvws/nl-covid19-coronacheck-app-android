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

abstract class ScanQrViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val validatedQrLiveData = MutableLiveData<Event<VerifiedQrResultState>>()

    abstract fun validate(qrContent: String)
    abstract fun scanInstructionsSeen(): Boolean
}

class ScanQrViewModelImpl(
    private val testResultValidUseCase: TestResultValidUseCase,
    private val persistenceManager: PersistenceManager
) : ScanQrViewModel() {

    override fun validate(qrContent: String) {
        loadingLiveData.value = Event(true)
        viewModelScope.launch {
            try {
                val result = testResultValidUseCase.validate(
                    qrContent = qrContent
                )
                validatedQrLiveData.value = Event(result)
            } finally {
                loadingLiveData.value = Event(false)
            }
        }
    }

    override fun scanInstructionsSeen(): Boolean {
        val seen = persistenceManager.getScanInstructionsSeen()
        if (!seen) {
            persistenceManager.setScanInstructionsSeen()
        }
        return seen
    }
}
