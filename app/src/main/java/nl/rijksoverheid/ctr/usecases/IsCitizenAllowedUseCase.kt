package nl.rijksoverheid.ctr.usecases

import android.util.Base64
import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.utils.KeyPair
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CustomerQr
import nl.rijksoverheid.ctr.citizen.models.Payload
import nl.rijksoverheid.ctr.citizen.util.EventUtil
import nl.rijksoverheid.ctr.data.models.AgentQR
import nl.rijksoverheid.ctr.ext.toObject
import nl.rijksoverheid.ctr.factories.KeyFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IsCitizenAllowedUseCase(
    private val lazySodium: LazySodiumAndroid,
    private val moshi: Moshi,
    private val eventUtil: EventUtil
) {

    sealed class IsCitizenAllowedResult {
        data class Allowed(val payload: Payload) : IsCitizenAllowedResult()
        data class NotAllowed(val reason: String) : IsCitizenAllowedResult()
    }

    fun isAllowed(
        customerQr: CustomerQr,
        agent: AgentQR.Agent
    ): IsCitizenAllowedResult {
        val eventPrivateKey = agent.event.privateKey
        val decryptionKey = KeyPair(
            KeyFactory.createKeyFromBase64String(customerQr.publicKey),
            KeyFactory.createKeyFromBase64String(eventPrivateKey)
        )

        val payloadDecrypted = lazySodium.cryptoBoxOpenEasy(
            customerQr.payload,
            Base64.decode(customerQr.nonce, Base64.NO_WRAP),
            decryptionKey
        )

        val payload = payloadDecrypted.toObject<Payload>(moshi)

        val checkValidTestResult = eventUtil.checkValidTestResult(
            event = agent.event,
            userTestResult = payload.test
        )

        return if (checkValidTestResult) {
            IsCitizenAllowedResult.Allowed(payload)
        } else {
            IsCitizenAllowedResult.NotAllowed("Citizen is not allowed")
        }
    }

}
