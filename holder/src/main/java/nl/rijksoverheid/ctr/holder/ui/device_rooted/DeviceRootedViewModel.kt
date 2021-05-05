package nl.rijksoverheid.ctr.holder.ui.device_rooted

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class DeviceRootedViewModel : ViewModel() {
    val deviceRootedLiveData = MutableLiveData<Event<Boolean>>()
    abstract fun setHasDismissedRootedDeviceDialog()
}

class DeviceRootedViewModelImpl(
    private val deviceRootedUseCase: DeviceRootedUseCase,
    private val persistenceManager: PersistenceManager
) :
    DeviceRootedViewModel() {

    init {
        viewModelScope.launch {
            // When the device rooted dialog has never been dismissed, keep checking if we are dealing
            // with a rooted device
            if (!persistenceManager.hasDismissedRootedDeviceDialog()) {
                deviceRootedLiveData.postValue(Event(deviceRootedUseCase.isDeviceRooted()))
            }
        }
    }

    override fun setHasDismissedRootedDeviceDialog() {
        persistenceManager.setHasDismissedRootedDeviceDialog()
    }
}

