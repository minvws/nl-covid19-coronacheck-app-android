package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.QrCodeUseCase

abstract class QrCodeViewModel : ViewModel() {
    val qrCodeLiveData = MutableLiveData<Bitmap>()
    abstract fun generateQrCode(size: Int, credential: ByteArray, shouldDisclose: Boolean)
}

class QrCodeViewModelImpl( private val qrCodeUseCase: QrCodeUseCase) : QrCodeViewModel() {

    override fun generateQrCode(size: Int, credential: ByteArray, shouldDisclose: Boolean) {
        viewModelScope.launch {
            val qrCodeBitmap = qrCodeUseCase.qrCode(
                credential = credential,
                qrCodeWidth = size,
                qrCodeHeight = size,
                shouldDisclose = shouldDisclose
            )
            qrCodeLiveData.postValue(qrCodeBitmap)
        }
    }
}