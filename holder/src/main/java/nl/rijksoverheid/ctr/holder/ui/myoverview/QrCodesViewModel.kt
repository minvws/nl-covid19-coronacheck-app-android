package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.QrCodesResultUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodesResult
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.ReturnToExternalAppUseCase

abstract class QrCodesViewModel : ViewModel() {
    val qrCodeDataListLiveData = MutableLiveData<QrCodesResult>()
    val returnAppLivedata = MutableLiveData<ExternalReturnAppData>()
    abstract fun generateQrCodes(
        greenCardType: GreenCardType,
        originType: OriginType,
        size: Int,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean
    )

    abstract fun onReturnUriGiven(uri: String, type: GreenCardType)
}

class QrCodesViewModelImpl(
    private val qrCodesResultUseCase: QrCodesResultUseCase,
    private val returnToExternalAppUseCase: ReturnToExternalAppUseCase
) : QrCodesViewModel() {

    override fun generateQrCodes(
        greenCardType: GreenCardType,
        originType: OriginType,
        size: Int,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean
    ) {

        viewModelScope.launch {
            qrCodeDataListLiveData.postValue(
                qrCodesResultUseCase.getQrCodesResult(
                    greenCardType = greenCardType,
                    originType = originType,
                    credentials = credentials,
                    shouldDisclose = shouldDisclose,
                    qrCodeWidth = size,
                    qrCodeHeight = size
                )
            )
        }
    }

    override fun onReturnUriGiven(uri: String, type: GreenCardType) {
        returnAppLivedata.postValue(returnToExternalAppUseCase.get(uri, type))
    }
}