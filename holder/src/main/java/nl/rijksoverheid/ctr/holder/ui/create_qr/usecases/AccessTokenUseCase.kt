package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AccessTokenUseCase {
    suspend fun token(providerIdentifier: String, bsn: String): RemoteAccessTokens.Token?
}

class AccessTokenUseCaseImpl(private val coronaCheckRepository: CoronaCheckRepository) :
    AccessTokenUseCase {

    override suspend fun token(providerIdentifier: String, bsn: String): RemoteAccessTokens.Token? {
        return coronaCheckRepository.accessTokens(bsn).tokens.firstOrNull { it.providerIdentifier == providerIdentifier }
    }
}
