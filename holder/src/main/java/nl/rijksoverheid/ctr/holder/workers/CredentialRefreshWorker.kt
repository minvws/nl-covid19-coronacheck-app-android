package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CredentialRefreshWorker(
    context: Context,
    params: WorkerParameters,
    configResultUseCase: ConfigResultUseCase,
    cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
) : ConfigFetchWorker(context, params, cachedAppConfigUseCase, configResultUseCase) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        when (val configWorkResult = super.doWork()) {
            Result.success() -> {
                credentialsRefresh()
            }
            else -> configWorkResult
        }
    }

    private suspend fun credentialsRefresh(): Result {
        return when (holderDatabaseSyncer.sync(
            flow = HolderFlow.SyncGreenCards,
            syncWithRemote = true
        )) {
            is DatabaseSyncerResult.Failed.Error -> Result.failure()
            is DatabaseSyncerResult.Failed.NetworkError -> Result.retry()
            is DatabaseSyncerResult.Failed.ServerError.FirstTime -> Result.retry()
            is DatabaseSyncerResult.Failed.ServerError.MultipleTimes -> Result.failure()
            is DatabaseSyncerResult.FuzzyMatchingError -> Result.success()
            is DatabaseSyncerResult.Success -> Result.success()
        }
    }

    companion object {
        const val uniqueWorkNameTag = "credentials_refresh_worker"
    }
}
