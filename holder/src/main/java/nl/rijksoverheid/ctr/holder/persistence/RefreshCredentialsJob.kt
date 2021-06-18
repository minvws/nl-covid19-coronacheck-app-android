package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.work.*
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class RefreshCredentialsJob(
    context: Context,
    params: WorkerParameters,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val greenCardsUseCase: GreenCardsUseCase,
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val expiringCardOriginType = greenCardsUseCase.expiringCardOriginType()
        val syncWithRemote = expiringCardOriginType != null
        return if (syncWithRemote) {
             when (holderDatabaseSyncer.sync(OriginType.getAsString(expiringCardOriginType!!), true)) {
                DatabaseSyncerResult.Success -> Result.success()
                else -> Result.retry()
            }
        } else {
            holderDatabaseSyncer.sync(
                syncWithRemote = false
            )
            Result.success()
        }


    }

    companion object {
        fun schedule(context: Context, credentialRenewalDays: Long) {
            val request = PeriodicWorkRequestBuilder<RefreshCredentialsJob>(credentialRenewalDays, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("refresh_credentials", ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}

