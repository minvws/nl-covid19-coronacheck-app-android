/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

sealed class RefreshState {
    class Refreshable(val days: Long) : RefreshState()
    object NoRefresh : RefreshState()
}

interface GreenCardRefreshUtil {
    suspend fun shouldRefresh(): Boolean
    suspend fun refreshState(): RefreshState
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

        // Foreign dccs should not be refreshed so exclude them from the refresh logic
        val greenCardsToRefresh = holderDatabase.greenCardDao().getAll()
            .filter { !greenCardUtil.isForeignDcc(it) }

        greenCardsToRefresh?.forEach { greenCard ->
            val hasNewCredentials = !greenCardUtil.getExpireDate(greenCard).isEqual(
                greenCard.credentialEntities.lastOrNull()?.expirationTime
                    ?: OffsetDateTime.now(clock)
            )
            val latestCredential = greenCard.credentialEntities.maxByOrNull { it.expirationTime }
            val latestCredentialExpiring = latestCredential?.let {
                credentialUtil.isExpiring(credentialRenewalDays, latestCredential)
            } ?: false

            println("CHECK -> greencard ${greenCard.greenCardEntity.type} $hasNewCredentials $latestCredentialExpiring")
        }

        val greenCardExpiring = greenCardsToRefresh.firstOrNull { greenCard ->
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
        val hasValidFutureOrigins = greenCardsToRefresh
            .filter { it.credentialEntities.isEmpty() }
            .any { greenCard ->
                greenCard.origins.any {
                    originUtil.isValidWithinRenewalThreshold(credentialRenewalDays, it)
                }
            }

        return greenCardExpiring != null || hasValidFutureOrigins
    }

    // returns the refresh state of the app,
    // if it should schedule a refresh or not
    // and if so, in how many days from now
    override suspend fun refreshState(): RefreshState {
        val credentialRenewalDays = holderConfig.credentialRenewalDays.toLong()

        val greenCardsToRefresh = holderDatabase.greenCardDao().getAll()
            .filter { !greenCardUtil.isForeignDcc(it) }

        // find the furthest in the future credentials that
        // can be renewed, if any
        val latestCredentialExpirationTime: OffsetDateTime? =
            greenCardsToRefresh.filter { greenCard ->
                    !greenCardUtil.getExpireDate(greenCard).isEqual(
                        greenCard.credentialEntities.lastOrNull()?.expirationTime
                            ?: OffsetDateTime.now(clock)
                    )
                }.mapNotNull { greenCard ->
                    greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.expirationTime
                }.maxOrNull()

        // for domestic, we don't get credentials for origins which are not valid yet
        // so we take into account to fetch them when they become valid
        val firstFutureValidFrom: OffsetDateTime? = greenCardsToRefresh
            .filter { it.credentialEntities.isEmpty() }
            .flatMap { originUtil.getOriginState(it.origins) }
            .filterIsInstance<OriginState.Future>()
            .map { it.origin.validFrom }
            .minOrNull()

        // either we have a future origin to refresh, or an expiring credentials or both
        // in the latter case, use the one closest to now
        return when {
            firstFutureValidFrom != null && latestCredentialExpirationTime != null -> {
                if (firstFutureValidFrom.isBefore(latestCredentialExpirationTime.minusDays(credentialRenewalDays))) {
                    RefreshState.Refreshable(
                        daysBetween(firstFutureValidFrom)
                    )
                } else {
                    RefreshState.Refreshable(
                        daysBetween(latestCredentialExpirationTime.minusDays(credentialRenewalDays))
                    )
                }
            }
            firstFutureValidFrom != null -> RefreshState.Refreshable(
                daysBetween(firstFutureValidFrom)
            )
            latestCredentialExpirationTime != null -> RefreshState.Refreshable(
                daysBetween(latestCredentialExpirationTime.minusDays(credentialRenewalDays))
            )
            else -> RefreshState.NoRefresh
        }
    }

    private fun daysBetween(toDate: OffsetDateTime): Long {
        val days = DAYS.between(OffsetDateTime.now(clock), toDate)
        return if (days < 1) {
            1
        } else {
            days
        }
    }
}
