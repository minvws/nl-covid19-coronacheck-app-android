package nl.rijksoverheid.ctr.holder.myoverview

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.usecase.QrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import java.time.OffsetDateTime

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

    fun getLocalTestResult() {
        localTestResultLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val localTestResult = localTestResultUseCase.get()
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

    suspend fun generateQrCode(credentials: String, qrCodeWidth: Int, qrCodeHeight: Int) {
        qrCodeLiveData.value = Result.Loading()
        while (true) {
            try {
                val qrCodeBitmap = qrCodeUseCase.qrCode(
                    credentials = credentials.toByteArray(),
                    qrCodeWidth = qrCodeWidth,
                    qrCodeHeight = qrCodeHeight
                )
                qrCodeLiveData.value = Result.Success(qrCodeBitmap)
            } catch (e: Exception) {
                qrCodeLiveData.value = Result.Failed(e)
            }
            delay(QrCodeUtil.VALID_FOR_SECONDS * 1000)
        }
    }
}

