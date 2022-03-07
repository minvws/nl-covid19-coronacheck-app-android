/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.qrcodes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodesResultUseCase
import nl.rijksoverheid.ctr.appconfig.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodesResult
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData

abstract class QrCodesViewModel : ViewModel() {
    val qrCodeDataListLiveData = MutableLiveData<QrCodesResult>()
    val returnAppLivedata = MutableLiveData<ExternalReturnAppData>()
    abstract fun generateQrCodes(
        greenCardType: GreenCardType,
        originType: OriginType,
        size: Int,
        credentials: List<ByteArray>,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose
    )

    abstract fun onReturnUriGiven(uri: String, type: GreenCardType)
}

class QrCodesViewModelImpl(
    private val qrCodesResultUseCase: QrCodesResultUseCase,
    private val returnToExternalAppUseCase: ReturnToExternalAppUseCase
) : QrCodesViewModel() {

    override fun generateQrCodes(
        greenCardType: GreenCardType,
        originType: OriginType,
        size: Int,
        credentials: List<ByteArray>,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose
    ) {

        viewModelScope.launch {
            qrCodeDataListLiveData.postValue(
                qrCodesResultUseCase.getQrCodesResult(
                    greenCardType = greenCardType,
                    originType = originType,
                    credentials = credentials,
                    shouldDisclose = shouldDisclose,
                    qrCodeWidth = size,
                    qrCodeHeight = size
                )
            )
        }
    }

    override fun onReturnUriGiven(uri: String, type: GreenCardType) {
        if (type == GreenCardType.Domestic) {
            val data = returnToExternalAppUseCase.get(uri)
            data?.let {
                returnAppLivedata.postValue(it)
            }
        }
    }
}