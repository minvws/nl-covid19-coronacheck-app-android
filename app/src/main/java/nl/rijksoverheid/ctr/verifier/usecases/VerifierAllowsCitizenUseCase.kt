package nl.rijksoverheid.ctr.verifier.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CitizenQr
import nl.rijksoverheid.ctr.citizen.util.EventUtil
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
class VerifierAllowsCitizenUseCase(
    private val moshi: Moshi,
    private val eventRepository: EventRepository,
    private val citizenAllowedUseCase: DecryptCitizenQrUseCase,
    private val signatureValidUseCase: SignatureValidUseCase,
    private val eventUtil: EventUtil
) {

    suspend fun allow(citizenQrContent: String): Boolean {
        val issuers = eventRepository.issuers()
        val citizenQr = citizenQrContent.toObject<CitizenQr>(moshi)
        val remoteAgent = eventRepository.remoteAgent("d9ff36de-2357-4fa6-a64e-1569aa57bf1c")

        val decryptedPayload = citizenAllowedUseCase.decrypt(
            citizenQr = citizenQr,
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
