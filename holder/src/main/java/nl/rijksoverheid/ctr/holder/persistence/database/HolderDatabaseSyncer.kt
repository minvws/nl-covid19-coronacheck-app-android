package nl.rijksoverheid.ctr.holder.persistence.database

import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.persistence.WorkerManagerWrapper
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCredentials
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderDatabaseSyncer {

    /**
     * Synchronized the database. Does cleanup in the database based on expiration dates and can resync with remote
     * @param expectedOriginType If not null checks if the remote credentials contain this origin. Will return [DatabaseSyncerResult.MissingOrigin] if it's not present.
     * @param syncWithRemote If true and the data call to resync succeeds, clear all green cards in the database and re-add them
     */
    suspend fun sync(expectedOriginType: OriginType? = null, syncWithRemote: Boolean = true): DatabaseSyncerResult
}

class HolderDatabaseSyncerImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val secretKeyUseCase: SecretKeyUseCase,
    private val workerManagerWrapper: WorkerManagerWrapper,
    private val greenCardUtil: GreenCardUtil
) : HolderDatabaseSyncer {

    override suspend fun sync(expectedOriginType: OriginType?, syncWithRemote: Boolean): DatabaseSyncerResult {
        return withContext(Dispatchers.IO) {
            val events = holderDatabase.eventGroupDao().getAll()

            // Check if we need to remove any events
            removeExpiredEventGroups(
                events = events
            )

            // Sync with remote
            if (syncWithRemote) {
                syncGreenCards(
                    events = events,
                    expectedOriginType = expectedOriginType
                )
            } else {
                DatabaseSyncerResult.Success
            }
        }
    }

    /**
     * Check if we need to remove events from the database
     */
    private suspend fun removeExpiredEventGroups(events: List<EventGroupEntity>) {
        events.forEach {
            val expireDate = when (it.type) {
                is OriginType.Vaccination -> {
                    cachedAppConfigUseCase.getCachedAppConfigVaccinationEventValidity()
                }
                is OriginType.Test -> {
                    cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours()
                }
                is OriginType.Recovery -> {
                    cachedAppConfigUseCase.getCachedAppConfigRecoveryEventValidity()
                }
            }

            if (it.maxIssuedAt.plusHours(expireDate.toLong()) <= OffsetDateTime.now()) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }

    @Transaction
    private suspend fun syncGreenCards(events: List<EventGroupEntity>, expectedOriginType: OriginType?): DatabaseSyncerResult {
        if (events.isNotEmpty()) {
            return try {
                val remoteCredentials = getRemoteCredentials(
                    events = events
                )

                if (expectedOriginType != null && !remoteCredentials.getAllOrigins().contains(expectedOriginType.getTypeString())) {
                    return DatabaseSyncerResult.MissingOrigin
                }

                // Remove all green cards from database
                removeAllGreenCards()

                // Create domestic green card with origins and credentials
                remoteCredentials.domesticGreencard?.let { remoteDomesticGreenCard ->
                    createDomesticGreenCards(
                        remoteDomesticGreenCard = remoteDomesticGreenCard
                    )
                }

                // Create european green card with origins and credentials
                remoteCredentials.euGreencards?.forEach { remoteEuropeanGreenCard ->
                    createEuropeanGreenCards(
                        remoteEuropeanGreenCard = remoteEuropeanGreenCard
                    )
                }
                DatabaseSyncerResult.Success
            } catch (e: HttpException) {
                DatabaseSyncerResult.ServerError(e.code())
            } catch (e: IOException) {
                val greenCards = holderDatabase.greenCardDao().getAll()
                DatabaseSyncerResult.NetworkError(
                    hasGreenCardsWithoutCredentials = greenCards
                        .any { greenCardUtil.hasNoActiveCredentials(it) }
                )
            } catch (e: Exception) {
                DatabaseSyncerResult.ServerError(200)
            } finally {
                workerManagerWrapper.scheduleNextCredentialsRefreshIfAny()
            }
        } else {
            return DatabaseSyncerResult.Success
        }
    }

    private suspend fun getRemoteCredentials(events: List<EventGroupEntity>): RemoteCredentials {
        val prepareIssue = coronaCheckRepository.getPrepareIssue()

        val commitmentMessage = mobileCoreWrapper.createCommitmentMessage(
            secretKey = secretKeyUseCase.json().toByteArray(),
            prepareIssueMessage = prepareIssue.prepareIssueMessage
        )

        return coronaCheckRepository.getCredentials(
            stoken = prepareIssue.stoken,
            events = events.map { String(it.jsonData) },
            issueCommitmentMessage = commitmentMessage
        )
    }

    private suspend fun removeAllGreenCards() {
        holderDatabase.greenCardDao().deleteAll()
        holderDatabase.originDao().deleteAll()
        holderDatabase.credentialDao().deleteAll()
    }

    private suspend fun createDomesticGreenCards(remoteDomesticGreenCard: RemoteCredentials.DomesticGreenCard) {
        // Create green card
        val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
            GreenCardEntity(
                walletId = 1,
                type = GreenCardType.Domestic
            )
        )

        // Create origins
        remoteDomesticGreenCard.origins.forEach { remoteOrigin ->
            val type = when (remoteOrigin.type) {
                OriginType.TYPE_VACCINATION -> OriginType.Vaccination
                OriginType.TYPE_RECOVERY -> OriginType.Recovery
                OriginType.TYPE_TEST -> OriginType.Test
                else -> throw IllegalStateException("Type not known")
            }
            holderDatabase.originDao().insert(
                OriginEntity(
                    greenCardId = localDomesticGreenCardId,
                    type = type,
                    eventTime = remoteOrigin.eventTime,
                    expirationTime = remoteOrigin.expirationTime,
                    validFrom = remoteOrigin.validFrom
                )
            )
        }

        // Create credentials
        val domesticCredentials = mobileCoreWrapper.createDomesticCredentials(
            createCredentials = remoteDomesticGreenCard.createCredentialMessages
        )

        val entities = domesticCredentials.map { domesticCredential ->
            CredentialEntity(
                greenCardId = localDomesticGreenCardId,
                data = domesticCredential.credential.toString().replace("\\/", "/").toByteArray(),
                credentialVersion = domesticCredential.attributes.credentialVersion,
                validFrom = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(domesticCredential.attributes.validFrom),
                    ZoneOffset.UTC
                ),
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(domesticCredential.attributes.validFrom),
                    ZoneOffset.UTC
                ).plusHours(domesticCredential.attributes.validForHours)
            )
        }

        holderDatabase.credentialDao().insertAll(entities)
    }

    private suspend fun createEuropeanGreenCards(remoteEuropeanGreenCard: RemoteCredentials.EuGreenCard) {
        // Create green card
        val localEuropeanGreenCardId = holderDatabase.greenCardDao().insert(
            GreenCardEntity(
                walletId = 1,
                type = GreenCardType.Eu
            )
        )

        // Create origins for european green card
        remoteEuropeanGreenCard.origins.forEach { remoteOrigin ->
            val type = when (remoteOrigin.type) {
                OriginType.TYPE_VACCINATION -> OriginType.Vaccination
                OriginType.TYPE_RECOVERY -> OriginType.Recovery
                OriginType.TYPE_TEST -> OriginType.Test
                else -> throw IllegalStateException("Type not known")
            }
            holderDatabase.originDao().insert(
                OriginEntity(
                    greenCardId = localEuropeanGreenCardId,
                    type = type,
                    eventTime = remoteOrigin.eventTime,
                    expirationTime = remoteOrigin.expirationTime,
                    validFrom = remoteOrigin.validFrom
                )
            )
        }

        // Create credential
        val europeanCredential = mobileCoreWrapper.readEuropeanCredential(
            credential = remoteEuropeanGreenCard.credential.toByteArray()
        )

        val entity = CredentialEntity(
            greenCardId = localEuropeanGreenCardId,
            data = remoteEuropeanGreenCard.credential.toByteArray(),
            credentialVersion = europeanCredential.getInt("credentialVersion"),
            validFrom = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(europeanCredential.getLong("issuedAt")),
                ZoneOffset.UTC
            ),
            expirationTime = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(europeanCredential.getLong("expirationTime")),
                ZoneOffset.UTC
            )
        )

        holderDatabase.credentialDao().insert(entity)
    }
}

sealed class DatabaseSyncerResult {
    object Success : DatabaseSyncerResult()
    object MissingOrigin : DatabaseSyncerResult()
    data class ServerError(val httpCode: Int, val errorCode: Int? = null) : DatabaseSyncerResult()
    data class NetworkError(val hasGreenCardsWithoutCredentials: Boolean) : DatabaseSyncerResult()
}
