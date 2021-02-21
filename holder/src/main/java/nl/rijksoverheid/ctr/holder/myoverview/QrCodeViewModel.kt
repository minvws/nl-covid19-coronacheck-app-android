package nl.rijksoverheid.ctr.holder.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.holder.usecase.QrCodeUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeViewModel(
    private val qrCodeUseCase: QrCodeUseCase,
) : ViewModel() {

    val qrCodeLiveData = MutableLiveData<Event<QrCodeData>>()

    suspend fun generateQrCode(localTestResult: LocalTestResult, qrCodeSize: Int) {
        while (true) {
            val qrCodeBitmap = qrCodeUseCase.qrCode(
                credentials = localTestResult.credentials.toByteArray(),
                qrCodeWidth = qrCodeSize,
                qrCodeHeight = qrCodeSize
            )
            qrCodeLiveData.value = Event(
                QrCodeData(
                    localTestResult = localTestResult,
                    qrCode = qrCodeBitmap
                )
            )
            delay(QrCodeUtil.VALID_FOR_SECONDS * 1000)
        }
    }

}

