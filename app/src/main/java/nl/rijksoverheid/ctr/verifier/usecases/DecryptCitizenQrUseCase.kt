package nl.rijksoverheid.ctr.verifier.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CitizenQr
import nl.rijksoverheid.ctr.citizen.models.CitizenQrPayload
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.models.Agent
import nl.rijksoverheid.ctr.shared.ext.toObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DecryptCitizenQrUseCase(
    private val moshi: Moshi,
    private val cryptoUtil: CryptoUtil
) {

    fun decrypt(
        citizenQr: CitizenQr,
        agent: Agent
    ): CitizenQrPayload {
        val eventPrivateKey = agent.event.privateKey

        val payloadDecrypted = cryptoUtil.boxOpenEasy(
            cipher = citizenQr.payload,
            nonce = citizenQr.nonce,
            publicKey = citizenQr.publicKey,
            privateKey = eventPrivateKey
        ) ?: throw Exception("Could not decrypt payload")

        return payloadDecrypted.toObject(moshi)
    }
}
