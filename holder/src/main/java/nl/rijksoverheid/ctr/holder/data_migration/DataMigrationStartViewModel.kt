package nl.rijksoverheid.ctr.holder.data_migration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class DataMigrationStartViewModel : ViewModel() {
    val canTransferData: LiveData<Event<Boolean>> = MutableLiveData()

    abstract fun canTransfer()
}

class DataMigrationStartViewModelImpl(
    private val dataMigrationUseCase: DataMigrationUseCase
) : DataMigrationStartViewModel() {
    override fun canTransfer() {
        viewModelScope.launch {
            (canTransferData as MutableLiveData).postValue(Event(dataMigrationUseCase.canTransferData()))
        }
    }
}
