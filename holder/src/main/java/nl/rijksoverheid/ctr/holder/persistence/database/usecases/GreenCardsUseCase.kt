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
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.isExpiring
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import java.time.Clock

interface GreenCardsUseCase {
    suspend fun expiringCardOriginType(): OriginType?
    suspend fun expiredCard(selectedType: GreenCardType): Boolean
    suspend fun lastExpiringCardTimeInDays(): Long?
}

class GreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
): GreenCardsUseCase {
    override suspend fun expiringCardOriginType(): OriginType? {

        val config = cachedAppConfigUseCase.getCachedAppConfig() ?: return null

        return holderDatabase.greenCardDao().getAll().firstOrNull { greenCard ->
            val minimumCredentialVersionIncreased = greenCard.credentialEntities.minByOrNull { it.credentialVersion }?.credentialVersion ?: 0 < config.minimumCredentialVersion
            val credentialExpiring = greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.isExpiring(config.credentialRenewalDays.toLong(), clock) ?: true
            minimumCredentialVersionIncreased || credentialExpiring
        }?.origins?.firstOrNull()?.type
    }

    override suspend fun expiredCard(selectedType: GreenCardType): Boolean {
        val allGreenCards = holderDatabase.greenCardDao().getAll()
        return allGreenCards.filter {
            it.greenCardEntity.type == selectedType
        }.any(greenCardUtil::isExpired)
    }

    override suspend fun lastExpiringCardTimeInDays(): Long? {
        val configCredentialRenewalDays = cachedAppConfigUseCase.getCachedAppConfig()?.credentialRenewalDays?.toLong() ?: 5L

        val lastExpiringGreenCard = holderDatabase.greenCardDao().getAll().mapNotNull { greenCard ->
            greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.expirationTime
        }.sortedByDescending { it.toEpochSecond() }.firstOrNull()

        return lastExpiringGreenCard?.minusDays(configCredentialRenewalDays)?.dayOfYear?.toLong()
    }
}
