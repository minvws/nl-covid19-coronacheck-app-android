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
import nl.rijksoverheid.ctr.shared.models.Environment
import java.util.concurrent.TimeUnit

interface WorkerManagerUtil {
    suspend fun scheduleRefreshCredentialsJob()
    fun cancelRefreshCredentialsJob(context: Context)
}

class WorkerManagerUtilImpl(
    private val context: Context,
    private val greenCardRefreshUtil: GreenCardRefreshUtil,
) : WorkerManagerUtil {

    val acc: Boolean = Environment.get(context) == Environment.Acc

    private val intervalUnit = if (acc) {
        TimeUnit.MINUTES
    } else {
        TimeUnit.DAYS
    }

    override suspend fun scheduleRefreshCredentialsJob() {
        val interval: Long = if (acc) {
            15
        } else {
            30
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<CredentialRefreshWorker>(
            repeatInterval = interval,
            repeatIntervalTimeUnit = intervalUnit)
            .setInitialDelay(greenCardRefreshUtil.credentialsExpireInDays(), intervalUnit)
            .setConstraints(constraints)
            .build()

        println("WM-GIO says schedule worker")
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                CredentialRefreshWorker.uniqueWorkNameTag,
                ExistingPeriodicWorkPolicy.REPLACE,
                request,
            )
    }

    override fun cancelRefreshCredentialsJob(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CredentialRefreshWorker.uniqueWorkNameTag)
    }
}