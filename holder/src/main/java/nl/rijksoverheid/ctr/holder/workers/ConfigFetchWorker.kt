package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class ConfigFetchWorker(
    private val context: Context,
    params: WorkerParameters,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase,
    private val configResultUseCase: ConfigResultUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val configResult = try {
            configResultUseCase.fetch()
        } catch (exception: Exception) {
            ConfigResult.Error(NetworkRequestResult.Failed.ClientNetworkError(HolderStep.ConfigurationNetworkRequest))
        }
        when (configResult) {
            is ConfigResult.Error -> {
                WorkManager.getInstance(context).cancelAllWork()
                Result.failure()
            }
            is ConfigResult.Success -> {
                val appDeactivated = cachedAppConfigUseCase.getCachedAppConfig().appDeactivated
                val appInArchiveMode = holderFeatureFlagUseCase.isInArchiveMode()
                if (appDeactivated || appInArchiveMode) {
                    WorkManager.getInstance(context).cancelAllWork()
                    Result.failure()
                } else {
                    Result.success()
                }
            }
        }
    }

    companion object {
        const val uniqueWorkNameTag = "fetch_config_worker"
    }
}
