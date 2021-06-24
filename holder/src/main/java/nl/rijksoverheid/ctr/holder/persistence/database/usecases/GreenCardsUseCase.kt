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
import nl.rijksoverheid.ctr.holder.persistence.database.entities.isExpiring
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS

interface GreenCardsUseCase {
    suspend fun expiring(): Boolean
    suspend fun expiredCard(selectedType: GreenCardType): Boolean
    suspend fun lastExpiringCard(): GreenCard
}

sealed class GreenCard {
    class Expiring(val refreshInDays: Long): GreenCard()
    object None: GreenCard()
}

class GreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
): GreenCardsUseCase {
    override suspend fun expiring(): Boolean {

        val config = cachedAppConfigUseCase.getCachedAppConfig() ?: return false

        return holderDatabase.greenCardDao().getAll().firstOrNull { greenCard ->
            val minimumCredentialVersionIncreased = greenCard.credentialEntities.minByOrNull { it.credentialVersion }?.credentialVersion ?: 0 < config.minimumCredentialVersion
            val credentialExpiring = greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.isExpiring(config.credentialRenewalDays.toLong(), clock) ?: true
            minimumCredentialVersionIncreased || credentialExpiring
        } != null
    }

    override suspend fun expiredCard(selectedType: GreenCardType): Boolean {
        val allGreenCards = holderDatabase.greenCardDao().getAll()
        return allGreenCards.filter {
            it.greenCardEntity.type == selectedType
        }.any(greenCardUtil::isExpired)
    }

    override suspend fun lastExpiringCard(): GreenCard {
        val configCredentialRenewalDays = cachedAppConfigUseCase.getCachedAppConfig()?.credentialRenewalDays?.toLong() ?: throw IllegalStateException("Invalid config file")

        val lastExpiringGreenCardRenewal = holderDatabase.greenCardDao().getAll()
            .mapNotNull { greenCard ->
                greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.expirationTime
            }.maxByOrNull { it.toEpochSecond() }?.minusDays(configCredentialRenewalDays)

       val now = OffsetDateTime.now(clock)

        return if (lastExpiringGreenCardRenewal != null) {
            val days = DAYS.between(now, lastExpiringGreenCardRenewal)
            GreenCard.Expiring(refreshInDays = if (days < 1) {
                1
            } else {
                days
            })
        } else {
            GreenCard.None
        }
    }
}
