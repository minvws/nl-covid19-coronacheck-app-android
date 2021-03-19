package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.models.QrCodeData
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.usecase.QrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class LocalTestResultViewModel : ViewModel() {

    val qrCodeLiveData = MutableLiveData<QrCodeData>()
    val localTestResultStateLiveData = MutableLiveData<Event<LocalTestResultState>>()
    val retrievedLocalTestResult: LocalTestResult?
        get() = (localTestResultStateLiveData.value?.peekContent() as? LocalTestResultState.Valid)?.localTestResult

    abstract fun getLocalTestResult()
    abstract fun generateQrCode(size: Int): Boolean
}

open class LocalTestResultViewModelImpl(
    private val secretKeyUseCase: SecretKeyUseCase,
    private val localTestResultUseCase: LocalTestResultUseCase,
    private val qrCodeUseCase: QrCodeUseCase
) : LocalTestResultViewModel() {

    override fun getLocalTestResult() {
        viewModelScope.launch {
            secretKeyUseCase.persist()
            val localTestResultState = localTestResultUseCase.get(localTestResultStateLiveData.value?.peekContent())
            localTestResultStateLiveData.value = Event(localTestResultState)
        }
    }

    /**
     * Generate a qr code from the local test result retrieved in [getLocalTestResult]
     * @param size The size of the qr code
     * @return If a qr code can be generated
     */
    override fun generateQrCode(size: Int): Boolean {
        retrievedLocalTestResult?.let {
            viewModelScope.launch {
                val qrCodeBitmap = qrCodeUseCase.qrCode(
                    credentials = it.credentials.toByteArray(),
                    qrCodeWidth = size,
                    qrCodeHeight = size
                )
                qrCodeLiveData.value =
                    QrCodeData(
                        localTestResult = it,
                        qrCode = qrCodeBitmap
                    )
            }
        }
        return retrievedLocalTestResult != null
    }
}
