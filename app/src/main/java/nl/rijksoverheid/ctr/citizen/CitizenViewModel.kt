package nl.rijksoverheid.ctr.citizen

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.citizen.models.CustomerQR
import nl.rijksoverheid.ctr.citizen.models.Payload
import nl.rijksoverheid.ctr.data.api.TestApiClient
import nl.rijksoverheid.ctr.data.models.EventQR
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.data.models.User
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
    private val moshi: Moshi,
    private val lazySodium: LazySodiumAndroid,
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

    fun generateQrCode(eventQrJson: String) {
        viewModelScope.launch {
            try {
                val eventQR = moshi.adapter(EventQR::class.java).fromJson(eventQrJson)
                    ?: throw Exception("EventQR could not be parsed")
                val testResults = api.getTestResults(getUserId())
                val positiveTestResult = testResults.testResults.first { it.result == 0 }
                val positiveTestSignature =
                    testResults.testSignatures.first { it.uuid == positiveTestResult.uuid }

                val keyPair = lazySodium.cryptoBoxKeypair()
                val nonce = lazySodium.nonce(SecretBox.NONCEBYTES)

                val payload = Payload(
                    eventUuid = eventQR.event.uuid,
                    time = System.currentTimeMillis() / 1000,
                    test = positiveTestResult,
                    testSignature = positiveTestSignature.signature
                )

                val encryptedPayloadBase64 = lazySodium.cryptoBoxEasy(
                    payload.toJson(moshi),
                    nonce,
                    KeyPair(Key.fromBase64String(eventQR.event.publicKey), keyPair.secretKey)
                )

                val customerQR = CustomerQR(
                    publicKey = Base64.encodeToString(keyPair.publicKey.asBytes, Base64.NO_WRAP),
                    nonce = Base64.encodeToString(nonce, Base64.NO_WRAP),
                    payload = encryptedPayloadBase64
                )

                val customerQRJson = customerQR.toJson(moshi)
                Timber.v("DONE")

            } catch (e: Exception) {

            }
        }
    }

}
