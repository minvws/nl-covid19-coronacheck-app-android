package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.PaperProofCodeResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.PaperProofCodeUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PaperProofCodeViewModel : ViewModel() {
    val codeResultLiveData: LiveData<Event<PaperProofCodeResult>> = MutableLiveData()
    abstract fun validateCode(code: String)
}

class PaperProofCodeViewModelImpl(private val paperProofCodeUseCase: PaperProofCodeUseCase): PaperProofCodeViewModel() {

    override fun validateCode(code: String) {
        (codeResultLiveData as MutableLiveData).postValue(Event(paperProofCodeUseCase.validate(code)))
    }
}
