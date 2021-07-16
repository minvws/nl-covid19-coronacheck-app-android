/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS

interface GreenCardRefreshUtil {
    suspend fun shouldRefresh(): Boolean
    suspend fun allCredentialsExpired(selectedType: GreenCardType): Boolean
    suspend fun credentialsExpireInDays(): Long
}

class GreenCardRefreshUtilImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
    private val credentialUtil: CredentialUtil,
) : GreenCardRefreshUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override suspend fun shouldRefresh(): Boolean {
        val credentialRenewalDays = holderConfig.credentialRenewalDays.toLong()

        val greenCardExpiring = holderDatabase.greenCardDao().getAll().firstOrNull { greenCard ->
            val hasNewCredentials = !greenCardUtil.getExpireDate(greenCard).isEqual(greenCard.credentialEntities.lastOrNull()?.expirationTime ?: OffsetDateTime.now(clock))
            val latestCredential = greenCard.credentialEntities.maxByOrNull { it.expirationTime }
            val latestCredentialExpiring = latestCredential?.let { credentialUtil.isExpiring(credentialRenewalDays, latestCredential) } ?: false
            hasNewCredentials && latestCredentialExpiring
        }

        return greenCardExpiring != null
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
            holderConfig.credentialRenewalDays.toLong()

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
