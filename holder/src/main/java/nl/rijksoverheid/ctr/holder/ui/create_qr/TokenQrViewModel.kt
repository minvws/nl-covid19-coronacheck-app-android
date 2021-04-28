/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TokenQrUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

class TokenQrViewModel(private val tokenQrUseCase: TokenQrUseCase) : ViewModel() {

    val locationData : LiveData<Event<TokenQrUseCase.TokenQrResult>> = MutableLiveData()

    fun checkLocationQrValidity(scannedData : String) {
        val data = tokenQrUseCase.checkValidQR(scannedData)
        (locationData as MutableLiveData).postValue(Event(data))
    }
}
