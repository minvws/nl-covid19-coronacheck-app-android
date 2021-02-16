package nl.rijksoverheid.ctr.holder.myoverview

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import nl.rijksoverheid.ctr.holder.usecase.QrCodeUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import timber.log.Timber

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

    val qrCodeLiveData = MutableLiveData<Event<Bitmap>>()

    suspend fun generateQrCode(credentials: String, qrCodeSize: Int) {
        while (true) {
            val qrCodeBitmap = qrCodeUseCase.qrCode(
                credentials = credentials.toByteArray(),
                qrCodeWidth = qrCodeSize,
                qrCodeHeight = qrCodeSize
            )
            qrCodeLiveData.value = Event(qrCodeBitmap)
            delay(QrCodeUtil.VALID_FOR_SECONDS * 1000)
        }
    }

}

