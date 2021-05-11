package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import clmobile.Clmobile
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

class CommitmentMessageUseCaseImpl(private val secretKeyUseCase: SecretKeyUseCase) :
    CommitmentMessageUseCase {

    override suspend fun json(nonce: String): String {
        val secretKey = secretKeyUseCase.json()
        return Clmobile.createCommitmentMessage(
            secretKey.toByteArray(),
            nonce.toByteArray()
        ).successString()
    }
}
