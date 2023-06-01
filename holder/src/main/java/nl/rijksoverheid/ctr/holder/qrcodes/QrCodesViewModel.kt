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
import nl.rijksoverheid.ctr.appconfig.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeAnimation
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodesResult
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeAnimationUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodesResultUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType

abstract class QrCodesViewModel : ViewModel() {
    val qrCodeDataListLiveData = MutableLiveData<QrCodesResult>()
    val returnAppLivedata = MutableLiveData<ExternalReturnAppData>()
    val animationLiveData = MutableLiveData<QrCodeAnimation>()
    abstract fun generateQrCodes(
        qrCodeFragmentData: QrCodeFragmentData,
        size: Int
    )

    abstract fun onReturnUriGiven(uri: String, type: GreenCardType)
    abstract fun getAnimation(greenCardType: GreenCardType)
}

class QrCodesViewModelImpl(
    private val qrCodesResultUseCase: QrCodesResultUseCase,
    private val returnToExternalAppUseCase: ReturnToExternalAppUseCase,
    private val qrCodeAnimationUseCase: QrCodeAnimationUseCase
) : QrCodesViewModel() {

    override fun generateQrCodes(
        qrCodeFragmentData: QrCodeFragmentData,
        size: Int
    ) {
        viewModelScope.launch {
            val result = qrCodesResultUseCase.getQrCodesResult(
                qrCodeFragmentData = qrCodeFragmentData,
                qrCodeWidth = size,
                qrCodeHeight = size
            )
            qrCodeDataListLiveData.postValue(
                result
            )
        }
    }

    override fun onReturnUriGiven(uri: String, type: GreenCardType) {
    }

    override fun getAnimation(greenCardType: GreenCardType) {
        animationLiveData.postValue(
            qrCodeAnimationUseCase.get(greenCardType)
        )
    }
}
