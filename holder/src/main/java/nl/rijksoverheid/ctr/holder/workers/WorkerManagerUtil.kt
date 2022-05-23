/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import nl.rijksoverheid.ctr.shared.models.Environment
import java.util.concurrent.TimeUnit

interface WorkerManagerUtil {
    fun scheduleConfigJob(lifecycleOwner: LifecycleOwner)
    fun cancelConfigJob(context: Context)
}

class WorkerManagerUtilImpl(
    private val context: Context,
): WorkerManagerUtil {

    val acc: Boolean = Environment.get(context) == Environment.Acc

    private val intervalMinutes: Long = if (acc) {
        15
    } else {
        36
    }

    private val intervalUnit = if (acc) {
        TimeUnit.MINUTES
    } else {
        TimeUnit.HOURS
    }

    override fun scheduleConfigJob(lifecycleOwner: LifecycleOwner) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<ConfigFetchJob>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = intervalUnit)
            .setConstraints(constraints)
            .build()

        println("WM-GIO says schedule worker")
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                ConfigFetchJob.uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                request,
            )
    }

    override fun cancelConfigJob(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ConfigFetchJob.uniqueWorkName)
    }
}