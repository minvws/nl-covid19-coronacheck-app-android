/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.isExpiring
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.GreenCardErrorState
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit.DAYS

typealias CardUiLogic = suspend () -> Unit

interface GreenCardsUseCase {
    suspend fun faultyVaccinationsJune28(): Boolean
    suspend fun expiring(): Boolean
    suspend fun expiredCard(selectedType: GreenCardType): Boolean
    suspend fun firstExpiringCard(): GreenCard
    suspend fun refresh(handleErrorOnExpiringCard: suspend (DatabaseSyncerResult) -> GreenCardErrorState,
                        showForcedError: CardUiLogic,
                        showRefreshError: CardUiLogic,
                        showCardLoading: CardUiLogic,
                        holderDatabaseSyncer: HolderDatabaseSyncer): GreenCardErrorState
}

sealed class GreenCard {
    class Expiring(val refreshInDays: Long) : GreenCard()
    object None : GreenCard()
}

class GreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
    private val persistenceManager: PersistenceManager,
    private val androidUtil: AndroidUtil,
) : GreenCardsUseCase {
    private val bugDate = OffsetDateTime.ofInstant(
        Instant.parse("2021-06-28T09:00:00.00Z"),
        ZoneId.of("UTC")
    )

    override suspend fun faultyVaccinationsJune28(): Boolean {
        return holderDatabase.greenCardDao().getAll()
            .filter { it.greenCardEntity.type == GreenCardType.Domestic }
            .any { greenCard ->
                val hasVaccinationAndTestOrigins = greenCard.origins.map { it.type }.containsAll(
                    setOf(
                        OriginType.Test, OriginType.Vaccination
                    )
                )
                val originsOlderThanBugDate =
                    greenCard.origins.any { it.eventTime.isBefore(bugDate) }

                hasVaccinationAndTestOrigins && originsOlderThanBugDate
            }
    }

    override suspend fun expiring(): Boolean {

        val config = cachedAppConfigUseCase.getCachedAppConfig() ?: return false

        return holderDatabase.greenCardDao().getAll().firstOrNull { greenCard ->
            val credentialExpiring = greenCard.credentialEntities.maxByOrNull { it.expirationTime }
                ?.isExpiring(config.credentialRenewalDays.toLong(), clock) ?: true
            credentialExpiring
        } != null
    }

    override suspend fun expiredCard(selectedType: GreenCardType): Boolean {
        val allGreenCards = holderDatabase.greenCardDao().getAll()
        return allGreenCards.filter {
            it.greenCardEntity.type == selectedType
        }.any(greenCardUtil::isExpired)
    }

    override suspend fun firstExpiringCard(): GreenCard {
        val configCredentialRenewalDays =
            cachedAppConfigUseCase.getCachedAppConfig()?.credentialRenewalDays?.toLong()
                ?: throw IllegalStateException("Invalid config file")

        val firstExpiringGreenCardRenewal = holderDatabase.greenCardDao().getAll()
            .mapNotNull { greenCard ->
                greenCard.credentialEntities.maxByOrNull { it.expirationTime }?.expirationTime
            }.minByOrNull { it.toEpochSecond() }?.minusDays(configCredentialRenewalDays)

        val now = OffsetDateTime.now(clock)

        return if (firstExpiringGreenCardRenewal != null) {
            val days = DAYS.between(now, firstExpiringGreenCardRenewal)
            GreenCard.Expiring(
                refreshInDays = if (days < 1) {
                    1
                } else {
                    days
                }
            )
        } else {
            GreenCard.None
        }
    }

    /**
     * Sync the database with remote
     * We need to communicate to the user some updates:
     * - On June 28 there was a bug in the backend cause of which we need to sync and let the user know
     * - If one of the green cards is expiring, we need to update them and the user has to wait for their new state
     */
    override suspend fun refresh(
        handleErrorOnExpiringCard: suspend (DatabaseSyncerResult) -> GreenCardErrorState,
        showForcedError: CardUiLogic,
        showRefreshError: CardUiLogic,
        showCardLoading: CardUiLogic,
        holderDatabaseSyncer: HolderDatabaseSyncer,
    ): GreenCardErrorState {
        return if (!androidUtil.isFirstInstall() && !persistenceManager.hasAppliedJune28Fix() && faultyVaccinationsJune28()) {
            showForcedError()

            val syncResult = holderDatabaseSyncer.sync(
                syncWithRemote = true
            )

            if (syncResult != DatabaseSyncerResult.Success) {
                showRefreshError()
            } else {
                persistenceManager.setJune28FixApplied(true)
            }

            GreenCardErrorState.None
        } else {
            if (expiring()) {
                showCardLoading()

                val syncResult = holderDatabaseSyncer.sync(
                    syncWithRemote = true,
                )

                handleErrorOnExpiringCard(syncResult)
            } else {
                holderDatabaseSyncer.sync(
                    syncWithRemote = false
                )
                GreenCardErrorState.None
            }
        }
    }
}
