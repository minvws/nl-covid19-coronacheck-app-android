/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.myoverview.models.LocationQrData
import nl.rijksoverheid.ctr.holder.usecase.CheckLocationQrUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

class CheckLocationQrViewModel(private val checkLocationQrUseCase: CheckLocationQrUseCase) : ViewModel() {

    val locationData : LiveData<Event<CheckLocationQrUseCase.QrCheckResult>> = MutableLiveData()

    fun checkLocationQrValidity(scannedData : String) {
        val data = checkLocationQrUseCase.checkValidQR(scannedData)
        (locationData as MutableLiveData).postValue(Event(data))
    }

    fun formatLocationCode(data : LocationQrData) : String {
        return "${data.providerIdentifier}-${data.token}"
    }
}