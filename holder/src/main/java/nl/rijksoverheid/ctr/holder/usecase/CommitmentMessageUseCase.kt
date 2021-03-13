package nl.rijksoverheid.ctr.holder.usecase

import clmobile.Clmobile
import nl.rijksoverheid.ctr.api.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.ext.successString

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CommitmentMessageUseCase {
    suspend fun json(nonce: String): String
}

class CommitmentMessageUseCaseImpl(
    private val secretKeyUseCase: SecretKeyUseCase,
    private val testResultRepository: TestResultRepository
) :
    CommitmentMessageUseCase {

    override suspend fun json(nonce: String): String {
        val secretKey = secretKeyUseCase.json()
        return Clmobile.createCommitmentMessage(
            secretKey.toByteArray(),
            testResultRepository.getIssuerPublicKey().toByteArray(),
            nonce.toByteArray()
        ).successString()
    }
}
