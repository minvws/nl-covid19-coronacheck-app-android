package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardRefreshUtil

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
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncWithRemote = greenCardRefreshUtil.shouldRefresh()
        return if (syncWithRemote) {
            when (holderDatabaseSyncer.sync(null, true)) {
                is DatabaseSyncerResult.Success -> {
                    Result.success()
                }
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
        private const val uniqueWorkName = "refresh_credentials"
    }
}

