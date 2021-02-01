package nl.rijksoverheid.ctr.verifier.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.HolderQr
import nl.rijksoverheid.ctr.holder.models.HolderQrPayload
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.models.Agent
import nl.rijksoverheid.ctr.shared.util.CryptoUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DecryptHolderQrUseCase(
    private val moshi: Moshi,
    private val cryptoUtil: CryptoUtil
) {

    fun decrypt(
        holderQr: HolderQr,
        agent: Agent
    ): HolderQrPayload {
        val eventPrivateKey = agent.event.privateKey

        val payloadDecrypted = cryptoUtil.boxOpenEasy(
            cipher = holderQr.payload,
            nonce = holderQr.nonce,
            publicKey = holderQr.publicKey,
            privateKey = eventPrivateKey
        ) ?: throw Exception("Could not decrypt payload")

        return payloadDecrypted.toObject(moshi)
    }
}
