package nl.rijksoverheid.ctr.holder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class HolderMainActivityViewModel : ViewModel() {
    val navigateLiveData: LiveData<Event<NavDirections>> = MutableLiveData()
    abstract fun navigate(navDirections: NavDirections, delayMillis: Long = 0)
}

class HolderMainActivityViewModelImpl : HolderMainActivityViewModel() {

    override fun navigate(navDirections: NavDirections, delayMillis: Long) {
        viewModelScope.launch {
            delay(delayMillis)
            (navigateLiveData as MutableLiveData).postValue(Event(navDirections))
        }
    }
}
