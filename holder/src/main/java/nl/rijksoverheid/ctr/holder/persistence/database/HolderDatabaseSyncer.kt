package nl.rijksoverheid.ctr.holder.persistence.database

import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCredentials
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
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
    suspend fun sync(): DatabaseSyncerResult
}

class HolderDatabaseSyncerImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val secretKeyUseCase: SecretKeyUseCase,
) : HolderDatabaseSyncer {

    override suspend fun sync(): DatabaseSyncerResult {
        return withContext(Dispatchers.IO) {
            removeExpiredEventGroups()
            syncGreenCards()
        }
    }

    /**
     * Check if we need to remove events from the database
     */
    private suspend fun removeExpiredEventGroups() {
        val events = holderDatabase.eventGroupDao().getAll()
        events.forEach {
            val expireDate =
                if (it.type == EventType.Vaccination) cachedAppConfigUseCase.getCachedAppConfigVaccinationEventValidity()
                    .toLong() else cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours()

            if (it.maxIssuedAt.plusHours(expireDate.toLong()) <= OffsetDateTime.now()) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }

    @Transaction
    private suspend fun syncGreenCards(): DatabaseSyncerResult {
        if (holderDatabase.eventGroupDao().getAll().isNotEmpty()) {
            return try {
                val remoteCredentials = getRemoteCredentials()

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
                DatabaseSyncerResult.NetworkError
            }
        } else {
            return DatabaseSyncerResult.Success
        }
    }

    private suspend fun getRemoteCredentials(): RemoteCredentials {
        val prepareIssue = coronaCheckRepository.getPrepareIssue()

        val commitmentMessage = mobileCoreWrapper.createCommitmentMessage(
            secretKey = secretKeyUseCase.json().toByteArray(),
            nonce = prepareIssue.prepareIssueMessage
        )

        return coronaCheckRepository.getCredentials(
            stoken = prepareIssue.stoken,
            events = "",
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
        val domesticCredentials = mobileCoreWrapper.getDomesticCredentials(
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
    }
}

sealed class DatabaseSyncerResult {
    object Success : DatabaseSyncerResult()
    data class ServerError(val httpCode: Int, val errorCode: Int? = null) : DatabaseSyncerResult()
    object NetworkError : DatabaseSyncerResult()
}
