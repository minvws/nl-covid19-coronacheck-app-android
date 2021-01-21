package nl.rijksoverheid.ctr.citizen

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.data.factory.DependencyFactory
import nl.rijksoverheid.ctr.data.models.EventQR
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.data.models.TestResults
import nl.rijksoverheid.ctr.data.models.User
import org.json.JSONObject
import timber.log.Timber
import kotlin.random.Random

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CitizenViewModel : ViewModel() {

    private val api = DependencyFactory().getTestApiClient()
    private val moshi = DependencyFactory().getMoshi()
    private val lazySodium = DependencyFactory().getSodium()
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

                val citizenEventKey = lazySodium.cryptoBoxKeypair()
                val citizenEventKeySecret = Key.fromBytes(
                    Base64.decode(
                        "qgWjkBGF6NhRRwSBr3otbP9xy/PB5bkfRoZfgCk4WQ0=",
                        Base64.NO_WRAP
                    )
                )
                val citizenEventKeyPublic = Key.fromBytes(
                    Base64.decode(
                        "o7sIGAXTJ57Lulaci9YIknI4WKIOIklke5kp1xQkjU4=",
                        Base64.NO_WRAP
                    )
                )

                val citizenEventNonce =
                    Key.fromBytes(Base64.decode("G8FXuTLjxWr4ZK3OYSbu/Q4UTUdc5i1h", Base64.NO_WRAP))
                val citizenEncryptionKey = KeyPair(
                    Key.fromBytes(Base64.decode(eventQR.event.publicKey, Base64.NO_WRAP)),
                    citizenEventKeySecret
                )

                val payload = JSONObject()
                payload.put("event_uuid", eventQR.event.uuid)
                payload.put("time", 0)
                payload.put(
                    "test",
                    moshi.adapter(TestResults.TestResult::class.java).toJson(positiveTestResult)
                )
                payload.put("test_signature", positiveTestSignature.signature)

                val customerQR = JSONObject()
                customerQR.put(
                    "public_key",
                    Base64.encodeToString(citizenEventKeyPublic.asBytes, Base64.NO_WRAP)
                )
                customerQR.put(
                    "nonce",
                    Base64.encodeToString(citizenEventNonce.asBytes, Base64.NO_WRAP)
                )
                customerQR.put(
                    "payload",
                    Base64.encodeToString(
                        lazySodium.cryptoSecretBoxEasy(
                            "{\"event_uuid\":\"d9ff36de-2357-4fa6-a64e-1569aa57bf1c\",\"time\":0,\"test\":{\"uuid\":\"d59da47f-2d76-43e6-9974-c58863b5a3e3\",\"test_type\":\"58d8e4b1-f890-4a2f-b810-0b775caa2149\",\"date_taken\":1611008913,\"result\":0},\"test_signature\":\"mvcbV29917q1OdJnAzXhIdgRLbQcZCGxLV\\/Ihe\\/1TQ4HsgP6YtAMR6iMLBYkfmjz7oa8DD420dPeHYxRxF75Cg==\"}",
                            citizenEventNonce.asBytes,
                            Key.fromBytes(
                                Base64.decode(
                                    "qgWjkBGF6NhRRwSBr3otbP9xy/PB5bkfRoZfgCk4WQ1eULXJuj/fJhCt3uDf4mFcNKT9AL93QsoblYpE5DsyTw==",
                                    Base64.NO_WRAP
                                )
                            )
                        ).toByteArray(), Base64.NO_WRAP
                    )
                )
                Timber.v("test")

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

}
