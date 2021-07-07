/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.isExpiring
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS

typealias CardUiLogic = suspend () -> Unit

interface GreenCardsUseCase {
    suspend fun shouldRefresh(): Boolean
    suspend fun allCredentialsExpired(selectedType: GreenCardType): Boolean
    suspend fun credentialsExpireInDays(): Long
}

class GreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
    private val credentialUtil: CredentialUtil,
) : GreenCardsUseCase {

    override suspend fun shouldRefresh(): Boolean {
        val credentialRenewalDays = cachedAppConfigUseCase.getCachedAppConfig()!!.credentialRenewalDays.toLong()

        return holderDatabase.greenCardDao().getAll().filterNot {
            // We don't need to refresh green cards that are about to expire since
            // there won't be any credentials to fetch for them
            greenCardUtil.isExpiring(credentialRenewalDays, it)
        }.firstOrNull { greenCard ->
            val credentialExpiring = greenCard.credentialEntities.maxByOrNull { it.expirationTime }
                ?.isExpiring(credentialRenewalDays, clock) ?: true
            credentialExpiring
        } != null
    }

    override suspend fun allCredentialsExpired(selectedType: GreenCardType): Boolean {
        val allGreenCards = holderDatabase.greenCardDao().getAll()
        return allGreenCards.filter {
            it.greenCardEntity.type == selectedType
        }.all {
            credentialUtil.getActiveCredential(it.credentialEntities) == null
        }
    }

    override suspend fun credentialsExpireInDays(): Long {
        val configCredentialRenewalDays =
            cachedAppConfigUseCase.getCachedAppConfig()?.credentialRenewalDays?.toLong()
                ?: throw IllegalStateException("Invalid config file")

        val firstExpiringGreenCardRenewal = holderDatabase.greenCardDao().getAll()
            .filterNot {
                greenCardUtil.isExpiring(configCredentialRenewalDays, it)
            }
            .mapNotNull { greenCard ->
                greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.expirationTime
            }.minByOrNull { it.toEpochSecond() }?.minusDays(configCredentialRenewalDays)

        val now = OffsetDateTime.now(clock)

        return if (firstExpiringGreenCardRenewal != null) {
            val days = DAYS.between(now, firstExpiringGreenCardRenewal)
            if (days < 1) {
                1
            } else {
                days
            }
        } else {
            0
        }
    }
}
