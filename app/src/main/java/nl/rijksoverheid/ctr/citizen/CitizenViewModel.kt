package nl.rijksoverheid.ctr.citizen

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.data.api.TestApiClient
import nl.rijksoverheid.ctr.data.models.EventQr
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.data.models.User
import nl.rijksoverheid.ctr.usecases.GenerateCitizenQrCodeUseCase
import nl.rijksoverheid.ctr.usecases.GetValidTestResultForEventUseCase
import nl.rijksoverheid.ctr.usecases.IsEventQrValidUseCase
import nl.rijksoverheid.ctr.usecases.IsTestResultSignatureValidUseCase
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CitizenViewModel(
    private val api: TestApiClient,
    private val isEventQrValidUseCase: IsEventQrValidUseCase,
    private val isTestResultValidUseCase: GetValidTestResultForEventUseCase,
    private val isTestResultSignatureValidUseCase: IsTestResultSignatureValidUseCase,
    private val generateCitizenQrCodeUseCase: GenerateCitizenQrCodeUseCase,
    private val moshi: Moshi,
) : ViewModel() {

    val userLiveData = MutableLiveData<Result<User>>()
    val qrCodeLiveData = MutableLiveData<Result<Bitmap>>()

    private fun getUserId(): String {
        val userResult = userLiveData.value
        if (userResult is Result.Success) {
            return userResult.data.id
        } else {
            throw IllegalStateException("User is not logged in")
        }
    }

    fun login() {
        viewModelScope.launch {
            try {
                userLiveData.postValue(Result.Success(User("ef9f409a-8613-4600-b135-8d2ac12559b3")))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun generateQrCode(eventQrJson: String, qrCodeWidth: Int, qrCodeHeight: Int) {
        viewModelScope.launch {
            try {
                val eventQR = moshi.adapter(EventQr::class.java).fromJson(eventQrJson)
                    ?: throw Exception("EventQR could not be parsed")
                val issuers = api.getIssuers().issuers

                val eventQrValidResult = isEventQrValidUseCase.isValid(
                    issuers = issuers,
                    eventQR = eventQR
                )

                if (eventQrValidResult is IsEventQrValidUseCase.EventQrValidResult.Invalid) {
                    throw Exception(eventQrValidResult.reason)
                }

                val testResultValidResult = isTestResultValidUseCase.isValid(
                    event = eventQR.event,
                    testResults = api.getTestResults(getUserId())
                )

                if (testResultValidResult is GetValidTestResultForEventUseCase.TestResultValidResult.Invalid) {
                    throw Exception(testResultValidResult.reason)
                }

                val validTestResult =
                    testResultValidResult as GetValidTestResultForEventUseCase.TestResultValidResult.Valid

                val testResultSignatureValidResult = isTestResultSignatureValidUseCase.isValid(
                    issuers = issuers,
                    validTestResultForEvent = validTestResult.testResult,
                    validTestResultSignature = validTestResult.testResultSignature
                )

                if (testResultSignatureValidResult is IsTestResultSignatureValidUseCase.IsTestResultValidResult.Invalid) {
                    throw Exception(testResultSignatureValidResult.reason)
                }

                val generateQrCodeResult = generateCitizenQrCodeUseCase.generateQrCode(
                    event = eventQR.event,
                    validTestResult = validTestResult.testResult,
                    validTestResultSignature = validTestResult.testResultSignature,
                    qrCodeWidth = qrCodeWidth,
                    qrCodeHeight = qrCodeHeight
                )

                if (generateQrCodeResult is GenerateCitizenQrCodeUseCase.GenerateCitizenQrCodeResult.Failed) {
                    throw Exception(generateQrCodeResult.reason)
                }

                val generateQrCodeSuccessResult =
                    (generateQrCodeResult as GenerateCitizenQrCodeUseCase.GenerateCitizenQrCodeResult.Success)

                qrCodeLiveData.postValue(Result.Success(generateQrCodeSuccessResult.bitmap))
            } catch (e: Exception) {
                qrCodeLiveData.postValue(Result.Failed(e))
            }
        }
    }
}

