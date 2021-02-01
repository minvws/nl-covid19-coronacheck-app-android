package nl.rijksoverheid.ctr.verifier.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.HolderQr
import nl.rijksoverheid.ctr.holder.util.EventUtil
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.repositories.EventRepository
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierAllowsHolderUseCase(
    private val moshi: Moshi,
    private val eventRepository: EventRepository,
    private val holderAllowedUseCase: DecryptHolderQrUseCase,
    private val signatureValidUseCase: SignatureValidUseCase,
    private val eventUtil: EventUtil
) {

    suspend fun allow(holderQrContent: String): Boolean {
        val issuers = eventRepository.issuers()
        val holderQr = holderQrContent.toObject<HolderQr>(moshi)
        val remoteAgent = eventRepository.remoteAgent("d9ff36de-2357-4fa6-a64e-1569aa57bf1c")

        val decryptedPayload = holderAllowedUseCase.decrypt(
            holderQr = holderQr,
            agent = remoteAgent.agent
        )

        signatureValidUseCase.checkValid(
            issuers = issuers.issuers,
            signature = decryptedPayload.testSignature,
            data = decryptedPayload.test
        )

        return eventUtil.allowedTestResult(
            event = remoteAgent.agent.event,
            userTestResult = decryptedPayload.test
        )
    }
}
