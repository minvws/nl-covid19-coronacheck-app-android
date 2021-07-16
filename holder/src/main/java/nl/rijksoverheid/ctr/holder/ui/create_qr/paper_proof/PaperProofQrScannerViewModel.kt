package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PaperProofQrScannerViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val validatePaperProofResultLiveData = MutableLiveData<Event<ValidatePaperProofResult>>()

    abstract fun validatePaperProof(qrContent: String, couplingCode: String)
}

class PaperProofQrScannerViewModelImpl(
    private val validatePaperProofUseCase: ValidatePaperProofUseCase
) : PaperProofQrScannerViewModel() {

    override fun validatePaperProof(qrContent: String, couplingCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            loadingLiveData.postValue(Event(true))
            val result = validatePaperProofUseCase.validate(
                qrContent = qrContent,
                couplingCode = couplingCode
            )
            validatePaperProofResultLiveData.postValue(Event(result))
            loadingLiveData.postValue(Event(false))
        }
    }
}