package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ConfigFetchJob(
    context: Context,
    params: WorkerParameters,
    private val appConfigViewModel: AppConfigViewModel,
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        println("GIO says Work work")
        return when (appConfigViewModel.fetch()) {
            ConfigResult.Error -> Result.retry()
            is ConfigResult.Success -> Result.success()
        }
    }

    companion object {
        const val uniqueWorkName = "fetch_config"
    }
}