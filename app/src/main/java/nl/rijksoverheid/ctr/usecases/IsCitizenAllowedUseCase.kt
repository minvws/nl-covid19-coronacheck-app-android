package nl.rijksoverheid.ctr.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CustomerQr
import nl.rijksoverheid.ctr.citizen.models.Payload
import nl.rijksoverheid.ctr.citizen.util.EventUtil
import nl.rijksoverheid.ctr.crypto.CryptoUtil
import nl.rijksoverheid.ctr.data.models.AgentQR
import nl.rijksoverheid.ctr.ext.toObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IsCitizenAllowedUseCase(
    private val moshi: Moshi,
    private val eventUtil: EventUtil,
    private val cryptoUtil: CryptoUtil
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

        val payloadDecrypted = cryptoUtil.boxOpenEasy(
            cipher = customerQr.payload,
            nonce = customerQr.nonce,
            publicKey = customerQr.publicKey,
            privateKey = eventPrivateKey
        ) ?: return IsCitizenAllowedResult.NotAllowed("Could not decrypt payload")

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
