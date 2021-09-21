/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.device_secure

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class DeviceSecureViewModel : ViewModel() {
    val deviceSecureLiveData = MutableLiveData<Event<Boolean>>()
    abstract fun setHasDismissedUnsecureDeviceDialog(value : Boolean)
}

class DeviceSecureViewModelImpl(
    private val deviceSecureUseCase: DeviceSecureUseCase,
    private val persistenceManager: PersistenceManager
) :
    DeviceSecureViewModel() {

    init {
        viewModelScope.launch {
            // When the device unsecured dialog has never been dismissed, keep checking if we are dealing
            // with an unsecured device
            // If user has dismissed the dialog before, but has since secured their device, reset the dialog
            // check so we can re-show it if security gets removed
            val isDeviceSecure = deviceSecureUseCase.isDeviceSecure()
            if (isDeviceSecure && persistenceManager.hasDismissedUnsecureDeviceDialog()){
                setHasDismissedUnsecureDeviceDialog(false)
            }
            if (!persistenceManager.hasDismissedUnsecureDeviceDialog()) {
                deviceSecureLiveData.postValue(Event(isDeviceSecure))
            }
        }
    }

    override fun setHasDismissedUnsecureDeviceDialog(value : Boolean) {
        persistenceManager.setHasDismissedUnsecureDeviceDialog(value)
    }

}