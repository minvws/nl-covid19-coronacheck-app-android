package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class ConfigFetchWorker(
    context: Context,
    params: WorkerParameters,
    private val configResultUseCase: ConfigResultUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        when (configResultUseCase.fetch()) {
            ConfigResult.Error -> Result.retry()
            is ConfigResult.Success -> Result.success()
        }
    }

    companion object {
        const val uniqueWorkNameTag = "fetch_config_worker"
    }
}
