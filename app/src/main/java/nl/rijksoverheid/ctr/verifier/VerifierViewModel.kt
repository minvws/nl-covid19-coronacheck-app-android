package nl.rijksoverheid.ctr.verifier

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.utils.KeyPair
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CustomerQR
import nl.rijksoverheid.ctr.citizen.models.Payload
import nl.rijksoverheid.ctr.data.models.AgentQR
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.ext.toObject
import nl.rijksoverheid.ctr.factories.KeyFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierViewModel(
    private val moshi: Moshi,
    private val lazySodium: LazySodiumAndroid
) : ViewModel() {

    val customerAllowedLiveData = MutableLiveData<Result<Boolean>>()

    fun validateCustomer(customerQrJson: String) {
        val customerQR = customerQrJson.toObject<CustomerQR>(moshi)
        val agentQR =
            "{\"agent\":{\"event\":{\"name\":\"Friday Night\",\"private_key\":\"BWbaGr8FH2w9ndg8fP0Q8uBafSMZPpY83eG7Ha6hD4w=\",\"valid_from\":1611008598,\"valid_to\":1611584139,\"type\":{\"uuid\":\"e2255ea4-2140-44c8-bdf0-33da60debf70\",\"name\":\"Friday Night\"},\"valid_tests\":[{\"name\":\"PCR\",\"uuid\":\"58d8e4b1-f890-4a2f-b810-0b775caa2149\",\"max_validity\":604800},{\"name\":\"Breathalyzer\",\"uuid\":\"e4ecba8d-1f87-4d72-b698-b3136e7c1141\",\"max_validity\":10800}]}},\"agent_signature\":\"jm4EJm7s9Xtx1N0SqMpgParF0N1IPsrbS\\/475DJPmaiIJXXwVhANRVfcXTZg2Hhrju512u8TxXrdypRf3Pq4Dg==\"}".toObject<AgentQR>(
                moshi
            )
        val eventPrivateKey = agentQR.agent.event.privateKey
        val decryptionKey = KeyPair(
            KeyFactory.createKeyFromBase64String(customerQR.publicKey),
            KeyFactory.createKeyFromBase64String(eventPrivateKey)
        )

        val payloadDecrypted = lazySodium.cryptoBoxOpenEasy(
            customerQR.payload,
            Base64.decode(customerQR.nonce, Base64.NO_WRAP),
            decryptionKey
        )

        val payload = payloadDecrypted.toObject<Payload>(moshi)
        val customerAllowed = payload.test.result == 0
        customerAllowedLiveData.postValue(Result.Success(customerAllowed))
    }
}
