package nl.rijksoverheid.ctr.holder

import android.os.Bundle
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
    val navigateWithBundleLiveData: LiveData<Event<Pair<Int, Bundle>>> = MutableLiveData()
    abstract fun navigate(navDirections: NavDirections, delayMillis: Long = 0)

    abstract fun navigateWithBundle(actionId: Int, bundle: Bundle)
}

class HolderMainActivityViewModelImpl : HolderMainActivityViewModel() {

    override fun navigate(navDirections: NavDirections, delayMillis: Long) {
        viewModelScope.launch {
            delay(delayMillis)
            (navigateLiveData as MutableLiveData).postValue(Event(navDirections))
        }
    }

    override fun navigateWithBundle(actionId: Int, bundle: Bundle) {
        viewModelScope.launch {
            (navigateWithBundleLiveData as MutableLiveData).postValue(Event(Pair(actionId, bundle)))
        }
    }
}
