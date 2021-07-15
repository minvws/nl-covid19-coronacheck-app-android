package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PaperProofQrScannerViewModel: ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val validatePaperProofResultLiveData = MutableLiveData<Event<ValidatePaperProofResult>>()

    abstract fun validatePaperProof(qrContent: String, couplingCode: String)
}

class PaperProofQrScannerViewModelImpl(
    private val validatePaperProofUseCase: ValidatePaperProofUseCase): PaperProofQrScannerViewModel() {

    override fun validatePaperProof(qrContent: String, couplingCode: String) {
        loadingLiveData.value = Event(true)
        viewModelScope.launch(Dispatchers.IO) {
            val result = validatePaperProofUseCase.validate(
                qrContent = qrContent,
                couplingCode = couplingCode
            )
            validatePaperProofResultLiveData.postValue(Event(result))
            loadingLiveData.value = Event(false)
        }
    }
}