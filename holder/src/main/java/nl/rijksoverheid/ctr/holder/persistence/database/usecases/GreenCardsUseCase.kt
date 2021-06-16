/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.isExpiring

interface GreenCardsUseCase {
    suspend fun expiringCardOriginType(): String?
}

class GreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
): GreenCardsUseCase {
    override suspend fun expiringCardOriginType() =
        holderDatabase.greenCardDao().getAll().firstOrNull { greenCard ->
            val config = cachedAppConfigUseCase.getCachedAppConfig()!!
            val minimumCredentialVersionIncreased = greenCard.credentialEntities.minByOrNull { it.credentialVersion }?.credentialVersion ?: 0 < config.minimumCredentialVersion
            val credentialExpiring = greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.isExpiring(config.credentialRenewalDays.toLong()) ?: true
            minimumCredentialVersionIncreased || credentialExpiring
        }?.origins?.firstOrNull()?.type.toString()
}
