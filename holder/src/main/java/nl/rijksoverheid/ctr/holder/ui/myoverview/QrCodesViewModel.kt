package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.QrCodeDataUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.ReturnToExternalAppUseCase

abstract class QrCodesViewModel : ViewModel() {
    val qrCodeDataListLiveData = MutableLiveData<List<QrCodeData>>()
    val returnAppLivedata = MutableLiveData<ExternalReturnAppData>()
    abstract fun generateQrCodes(
        type: GreenCardType,
        size: Int,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean
    )

    abstract fun onReturnUriGiven(uri: String, type: GreenCardType)
}

class QrCodesViewModelImpl(
    private val qrCodeDataUseCase: QrCodeDataUseCase,
    private val returnToExternalAppUseCase: ReturnToExternalAppUseCase
) : QrCodesViewModel() {

    override fun generateQrCodes(
        type: GreenCardType,
        size: Int,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean
    ) {

        viewModelScope.launch {
            val qrCodeDataList = credentials.map {
                qrCodeDataUseCase.getQrCodeData(
                    greenCardType = type,
                    credential = it,
                    qrCodeWidth = size,
                    qrCodeHeight = size,
                    shouldDisclose = shouldDisclose
                )
            }

            qrCodeDataListLiveData.postValue(qrCodeDataList)
        }
    }

    override fun onReturnUriGiven(uri: String, type: GreenCardType) {
        returnAppLivedata.postValue(returnToExternalAppUseCase.get(uri, type))
    }
}