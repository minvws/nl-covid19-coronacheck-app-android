/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
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
    cachedAppConfigUseCase: HolderCachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
    private val credentialUtil: CredentialUtil,
    private val originUtil: OriginUtil
) : GreenCardRefreshUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override suspend fun shouldRefresh(): Boolean {
        val credentialRenewalDays = holderConfig.credentialRenewalDays.toLong()

        val greenCards = holderDatabase.greenCardDao().getAll()

        val greenCardExpiring = greenCards.firstOrNull { greenCard ->
            val hasNewCredentials = !greenCardUtil.getExpireDate(greenCard).isEqual(
                greenCard.credentialEntities.lastOrNull()?.expirationTime
                    ?: OffsetDateTime.now(clock)
            )
            val latestCredential = greenCard.credentialEntities.maxByOrNull { it.expirationTime }
            val latestCredentialExpiring = latestCredential?.let {
                credentialUtil.isExpiring(credentialRenewalDays, latestCredential)
            } ?: false
            hasNewCredentials && latestCredentialExpiring
        }

        // It can be that a green card has no credentials but they will be available in the future.
        // A refresh should be done in the case there are valid origins within the threshold.
        val hasValidFutureOrigins = greenCards
            .filter { it.credentialEntities.isEmpty() }
            .any { greenCard ->
                greenCard.origins.any {
                    originUtil.isValidWithinRenewalThreshold(credentialRenewalDays, it)
                }
            }

        return greenCardExpiring != null || hasValidFutureOrigins
    }

    override suspend fun allCredentialsExpired(selectedType: GreenCardType): Boolean {
        val allGreenCards = holderDatabase.greenCardDao().getAll()
        return allGreenCards.filter {
            it.greenCardEntity.type == selectedType
        }.all {
            credentialUtil.getActiveCredential(it.greenCardEntity.type, it.credentialEntities) == null
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
