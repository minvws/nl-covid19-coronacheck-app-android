package nl.rijksoverheid.ctr.verifier.ui.scanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class ScannerViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val verifiedQrResultStateLiveData = MutableLiveData<Event<VerifiedQrResultState>>()

    abstract fun validate(qrContent: String)
}

class ScannerViewModelImpl(private val testResultValidUseCase: TestResultValidUseCase) :
    ScannerViewModel() {
    override fun validate(qrContent: String) {
        loadingLiveData.value = Event(true)
        viewModelScope.launch {
            try {
                val result = testResultValidUseCase.validate(
                    qrContent = qrContent
                )
                verifiedQrResultStateLiveData.value = Event(result)
            } finally {
                loadingLiveData.value = Event(false)
            }
        }
    }

}
