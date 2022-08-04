package nl.rijksoverheid.ctr.verifier.scanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.VerificationResult
import nl.rijksoverheid.ctr.verifier.scanlog.usecase.LogScanUseCase
import nl.rijksoverheid.ctr.verifier.scanner.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.scanner.usecases.TestResultValidUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class ScannerViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val qrResultLiveData =
        MutableLiveData<Event<Pair<VerifiedQrResultState, ExternalReturnAppData?>>>()

    abstract fun log()
    abstract fun validate(qrContent: String, returnUri: String?, previousScanResult: VerificationResult? = null)
}

class ScannerViewModelImpl(
    private val testResultValidUseCase: TestResultValidUseCase,
    private val returnToExternalAppUseCase: ReturnToExternalAppUseCase,
    private val logScanUseCase: LogScanUseCase
) : ScannerViewModel() {

    override fun log() {
        viewModelScope.launch {
            logScanUseCase.log()
        }
    }

    override fun validate(qrContent: String, returnUri: String?, previousScanResult: VerificationResult?) {
        loadingLiveData.value = Event(true)
        viewModelScope.launch {
            try {
                val result = testResultValidUseCase.validate(
                    qrContent = qrContent
                )
                val externalReturnAppData = returnUri?.let { returnToExternalAppUseCase.get(it) }
                qrResultLiveData.value = Event(result to externalReturnAppData)
            } finally {
                loadingLiveData.value = Event(false)
            }
        }
    }
}
