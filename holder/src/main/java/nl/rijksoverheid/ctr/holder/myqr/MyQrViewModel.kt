package nl.rijksoverheid.ctr.holder.myqr

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.usecase.HolderQrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.models.Result

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyQrViewModel(
    private val secretKeyUseCase: SecretKeyUseCase,
    private val holderQrCodeUseCase: HolderQrCodeUseCase,
) : ViewModel() {

    val qrCodeLiveData = MutableLiveData<Result<Bitmap>>()

    init {
        viewModelScope.launch {
            secretKeyUseCase.persist()
        }
    }

    fun generateQrCode(accessToken: String, qrCodeWidth: Int, qrCodeHeight: Int) {
        qrCodeLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val qrCodeBitmap = holderQrCodeUseCase.qrCode(
                    accessToken = accessToken,
                    qrCodeWidth = qrCodeWidth,
                    qrCodeHeight = qrCodeHeight
                )
                qrCodeLiveData.postValue(Result.Success(qrCodeBitmap))
            } catch (e: Exception) {
                qrCodeLiveData.postValue(Result.Failed(e))
            }
        }
    }
}

