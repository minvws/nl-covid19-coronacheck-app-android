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
    open var codeResult: PaperProofCodeResult = PaperProofCodeResult.None

    val viewState: LiveData<ViewState> = MutableLiveData(ViewState())

    abstract fun validateCode()
    abstract fun updateViewState()
}

class PaperProofCodeViewModelImpl(private val savedStateHandle: SavedStateHandle,
                                  private val paperProofCodeUseCase: PaperProofCodeUseCase): PaperProofCodeViewModel() {

    override var code: String = savedStateHandle["code"] ?: ""
        set(value) {
            field = value
            savedStateHandle["verification_code"] = value
            updateViewState()
        }

    override var codeResult: PaperProofCodeResult = savedStateHandle["paper_proof_code_result"] ?: PaperProofCodeResult.None
        set(value) {
            field = value
            savedStateHandle["paper_proof_code_result"] = value
            updateViewState()
        }

    private val currentViewState: ViewState
        get() = viewState.value!!

    init {
        updateViewState()
    }

    override fun validateCode() {
        codeResult = paperProofCodeUseCase.validate(code)
        updateViewState()
    }

    override fun updateViewState() {
        (viewState as MutableLiveData).value = currentViewState.copy(
            buttonEnabled = code.isNotEmpty(),
            codeResult = codeResult
        )
    }
}

data class ViewState(
    val codeResult: PaperProofCodeResult = PaperProofCodeResult.None,
    val buttonEnabled: Boolean = false,
)
