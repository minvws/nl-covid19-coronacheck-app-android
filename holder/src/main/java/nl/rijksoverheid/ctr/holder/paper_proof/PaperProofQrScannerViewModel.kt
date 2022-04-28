package nl.rijksoverheid.ctr.holder.paper_proof

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofType
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetPaperProofTypeUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PaperProofQrScannerViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val paperProofTypeLiveData = MutableLiveData<Event<PaperProofType>>()

    abstract fun getType(qrContent: String)
}

class PaperProofQrScannerViewModelImpl(
    private val getPaperProofTypeUseCase: GetPaperProofTypeUseCase
) : PaperProofQrScannerViewModel() {

    override fun getType(qrContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            loadingLiveData.postValue(Event(true))
            val result = getPaperProofTypeUseCase.get(
                qrContent = qrContent
            )
            paperProofTypeLiveData.postValue(Event(result))
            loadingLiveData.postValue(Event(false))
        }
    }
}