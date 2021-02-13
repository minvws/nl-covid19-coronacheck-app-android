package nl.rijksoverheid.ctr.holder.myoverview

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.ext.tickFlow
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.usecase.QrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.models.Result
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeViewModel(
    secretKeyUseCase: SecretKeyUseCase,
    private val qrCodeUseCase: QrCodeUseCase,
    private val localTestResultUseCase: LocalTestResultUseCase
) : ViewModel() {

    val localTestResultLiveData = MutableLiveData<Result<LocalTestResult?>>()
    val qrCodeLiveData = MutableLiveData<Result<Bitmap>>()

    init {
        secretKeyUseCase.persist()
    }

    fun getLocalTestResult(currentDateTime: OffsetDateTime) {
        localTestResultLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val localTestResult = localTestResultUseCase.get(currentDateTime)
                withContext(Dispatchers.Main) {
                    localTestResultLiveData.value = Result.Success(localTestResult)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    localTestResultLiveData.value = Result.Failed(e)
                }
            }
        }
    }

    fun generateQrCode(credentials: String, qrCodeWidth: Int, qrCodeHeight: Int) {
        qrCodeLiveData.value = Result.Loading()
        viewModelScope.launch {
            tickFlow(TimeUnit.MINUTES.toMillis(3)).collect {
                try {
                    val qrCodeBitmap = qrCodeUseCase.qrCode(
                        credentials = credentials.toByteArray(),
                        qrCodeWidth = qrCodeWidth,
                        qrCodeHeight = qrCodeHeight
                    )
                    withContext(Dispatchers.Main) {
                        qrCodeLiveData.value = Result.Success(qrCodeBitmap)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        qrCodeLiveData.value = Result.Failed(e)
                    }
                }
            }
        }
    }
}

