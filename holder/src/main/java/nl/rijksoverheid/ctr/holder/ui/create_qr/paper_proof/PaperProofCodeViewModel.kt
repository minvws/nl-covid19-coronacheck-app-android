package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.PaperProofCodeResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.PaperProofCodeUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PaperProofCodeViewModel : ViewModel() {
    open var code: String = ""

    val codeResultLiveData: LiveData<Event<PaperProofCodeResult>> = MutableLiveData()
    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())

    abstract fun validateCode()
    abstract fun updateViewState()
}

class PaperProofCodeViewModelImpl(private val savedStateHandle: SavedStateHandle,
                                  private val paperProofCodeUseCase: PaperProofCodeUseCase): PaperProofCodeViewModel() {

    override var code: String = savedStateHandle["code"] ?: ""
        set(value) {
            field = value
            savedStateHandle["code"] = value
            updateViewState()
        }

    private val currentViewState: ViewState
        get() = viewState.value!!

    override fun validateCode() {
        (codeResultLiveData as MutableLiveData).postValue(Event(paperProofCodeUseCase.validate(code)))
        updateViewState()
    }

    override fun updateViewState() {
        (viewState as MutableLiveData).value = currentViewState.copy(
            buttonEnabled = code.isNotEmpty()
        )
    }
}

data class ViewState(
    val buttonEnabled: Boolean = false,
)
