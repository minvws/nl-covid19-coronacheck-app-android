package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetEventsUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class GetEventsViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val eventsResult: LiveData<Event<EventsResult>> = MutableLiveData()

    abstract fun getEvents(jwt: String, originType: OriginType)
}

class GetEventsViewModelImpl(
    private val eventUseCase: GetEventsUseCase
) : GetEventsViewModel() {

    override fun getEvents(jwt: String, originType: OriginType) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                val events = when (originType) {
                    is OriginType.Test -> {
                        eventUseCase.getEvents(
                            jwt = jwt,
                            originType = originType,
                            targetProviderIds = listOf(RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_GGD)
                        )
                    }
                    is OriginType.Vaccination -> {
                        eventUseCase.getEvents(
                            jwt = jwt,
                            originType = originType
                        )
                    }
                    is OriginType.Recovery -> {
                        eventUseCase.getEvents(
                            jwt = jwt,
                            originType = originType,
                            targetProviderIds = listOf(RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_GGD)
                        )
                    }
                }

                (eventsResult as MutableLiveData).value = Event(events)
            } finally {
                loading.value = Event(false)
            }
        }
    }
}