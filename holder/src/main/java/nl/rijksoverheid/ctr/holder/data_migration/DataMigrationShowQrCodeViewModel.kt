package nl.rijksoverheid.ctr.holder.data_migration

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeUseCase

abstract class DataMigrationShowQrCodeViewModel : ViewModel() {
    val qrCodesLiveData: LiveData<List<Bitmap>> = MutableLiveData()

    abstract fun generateQrCodes(size: Int)
}

class DataMigrationShowQrCodeViewModelImpl(
    private val dataExportUseCase: DataExportUseCase,
    private val qrCodeUseCase: QrCodeUseCase
) : DataMigrationShowQrCodeViewModel() {
    override fun generateQrCodes(size: Int) {
        viewModelScope.launch {
            val qrCodes = dataExportUseCase.export()
            val bitmaps = qrCodes.map {
                qrCodeUseCase.qrCode(
                    it.toByteArray(),
                    QrCodeFragmentData.ShouldDisclose.DoNotDisclose,
                    size,
                    size,
                    ErrorCorrectionLevel.M
                )
            }
            (qrCodesLiveData as MutableLiveData).postValue(bitmaps)
        }
    }
}
