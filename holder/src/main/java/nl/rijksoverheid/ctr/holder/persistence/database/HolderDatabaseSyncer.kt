package nl.rijksoverheid.ctr.holder.persistence.database

import androidx.room.Transaction
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderDatabaseSyncer {
    suspend fun sync(syncWithRemote: Boolean = false): DatabaseSyncerResult
}

class HolderDatabaseSyncerImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val coronaCheckRepository: CoronaCheckRepository
) : HolderDatabaseSyncer {

    override suspend fun sync(syncWithRemote: Boolean): DatabaseSyncerResult {
        removeExpiredEventGroups()

        return if (syncWithRemote) {
            syncGreenCards()
        } else {
            DatabaseSyncerResult.Success
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
        return try {
            val remoteCredentials = coronaCheckRepository.getCredentials()
            Timber.v(
                "Remote credentials: $remoteCredentials"
            )

            // Clear database entries
            holderDatabase.greenCardDao().deleteAll()
            holderDatabase.originDao().deleteAll()
            holderDatabase.credentialDao().deleteAll()

            // Create domestic green card with origins and credentials
            remoteCredentials.domesticGreencard?.let { remoteDomesticGreenCard ->

                // Create domestic green card
                val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
                    GreenCardEntity(
                        walletId = 1,
                        type = GreenCardType.Domestic
                    )
                )

                // Create origins for domestic green card
                remoteDomesticGreenCard.origin.forEach { remoteOrigin ->
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
                            expirationTime = remoteOrigin.expirationTime
                        )
                    )
                }

                // Create credentials for domestic green card
                // TODO Read with createCredentials, insert dummy credential for now
                holderDatabase.credentialDao().insert(
                    entity = CredentialEntity(
                        greenCardId = localDomesticGreenCardId,
                        data = "Dummy Data",
                        credentialVersion = 1
                    )
                )
            }

            // Create european green card with origins and credentials
            remoteCredentials.euGreencards?.forEach { remoteEuropeanGreenCard ->

                // Create european green card
                val localEuropeanGreenCardId = holderDatabase.greenCardDao().insert(
                    GreenCardEntity(
                        walletId = 1,
                        type = GreenCardType.Eu
                    )
                )

                // Create origins for domestic green card
                val remoteOrigin = remoteEuropeanGreenCard.origin
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
                        expirationTime = remoteOrigin.expirationTime
                    )
                )

                // Create credentials for domestic green card
                // TODO Read with createCredentials, insert dummy credential for now
                holderDatabase.credentialDao().insert(
                    entity = CredentialEntity(
                        greenCardId = localEuropeanGreenCardId,
                        data = "Dummy Data",
                        credentialVersion = 1
                    )
                )
            }

            DatabaseSyncerResult.Success
        } catch (e: HttpException) {
            DatabaseSyncerResult.ServerError(e.code())
        } catch (e: IOException) {
            DatabaseSyncerResult.NetworkError
        }
    }
}

sealed class DatabaseSyncerResult {
    object Success : DatabaseSyncerResult()
    data class ServerError(val httpCode: Int, val errorCode: Int? = null) : DatabaseSyncerResult()
    object NetworkError : DatabaseSyncerResult()
}
