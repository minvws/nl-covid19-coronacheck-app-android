/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.*
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.RefreshState
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.models.Environment
import java.util.concurrent.TimeUnit

interface WorkerManagerUtil {
    suspend fun scheduleRefreshCredentialsJob()
    fun cancelRefreshCredentialsJob(context: Context)
}

class WorkerManagerUtilImpl(
    private val context: Context,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
    private val appConfigUseCase: HolderCachedAppConfigUseCase,
) : WorkerManagerUtil {

    val acc: Boolean = Environment.get(context) == Environment.Acc

    // for testing, use minutes in acc builds
    private val intervalUnit = if (acc) {
        TimeUnit.MINUTES
    } else {
        TimeUnit.DAYS
    }

    private fun interval(): Long = if (acc) {
        15
    } else {
        appConfigUseCase.getCachedAppConfig().internationalQRRelevancyDays.toLong()
    }

    override suspend fun scheduleRefreshCredentialsJob() {
        val refreshState = greenCardRefreshUtil.refreshState()
        if (refreshState is RefreshState.Refreshable) {
            val credentialsRefreshInDays = refreshState.days

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<CredentialRefreshWorker>(
                repeatInterval = interval(),
                repeatIntervalTimeUnit = intervalUnit)
                .setInitialDelay(credentialsRefreshInDays, intervalUnit)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    CredentialRefreshWorker.uniqueWorkNameTag,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request,
                )
        }
    }

    override fun cancelRefreshCredentialsJob(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CredentialRefreshWorker.uniqueWorkNameTag)
    }
}