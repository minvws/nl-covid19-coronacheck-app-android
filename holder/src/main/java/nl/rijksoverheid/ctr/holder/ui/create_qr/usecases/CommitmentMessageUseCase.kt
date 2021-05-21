package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.shared.ClmobileWrapper

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

class CommitmentMessageUseCaseImpl(private val secretKeyUseCase: SecretKeyUseCase, private val clmobileWrapper: ClmobileWrapper) :
    CommitmentMessageUseCase {

    override suspend fun json(nonce: String): String {
        val secretKey = secretKeyUseCase.json()
        return clmobileWrapper.createCommitmentMessage(
            secretKey.toByteArray(),
            nonce.toByteArray()
        )
    }
}
