package nl.rijksoverheid.ctr.holder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofResult
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class HolderMainActivityViewModel: ViewModel() {
    /**
     * For when we need to communicate events between different navigations (holder_nav_graph_root and holder_nav_graph_main)
     */
    val eventsLiveData: LiveData<Event<Map<RemoteProtocol3, ByteArray>>> = MutableLiveData()

    /**
     * For when we need to communicate paper proof event errors between different navigations (holder_nav_graph_root and holder_nav_graph_main)
     */
    val validatePaperProofError: LiveData<Event<ValidatePaperProofResult.Invalid>> = MutableLiveData()

    abstract fun sendEvents(events: Map<RemoteProtocol3, ByteArray>)
    abstract fun sendValidatePaperProofInvalid(result: ValidatePaperProofResult.Invalid)
}

class HolderMainActivityViewModelImpl: HolderMainActivityViewModel() {

    override fun sendEvents(events: Map<RemoteProtocol3, ByteArray>) {
        (eventsLiveData as MutableLiveData).postValue(Event(events))
    }

    override fun sendValidatePaperProofInvalid(result: ValidatePaperProofResult.Invalid) {
        (validatePaperProofError as MutableLiveData).postValue(Event(result))
    }
}