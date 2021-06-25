package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetEventsUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class GetRecoveryViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val eventsResult: LiveData<Event<EventsResult>> = MutableLiveData()

    abstract fun getEvents(jwt: String)
}

class GetRecoveryViewModelImpl(
    private val eventUseCase: GetEventsUseCase
) : GetRecoveryViewModel() {

    override fun getEvents(jwt: String) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                (eventsResult as MutableLiveData).value =
                    Event(eventUseCase.getPositiveAndRecoveryEvents(jwt))
            } finally {
                loading.value = Event(false)
            }
        }
    }
}