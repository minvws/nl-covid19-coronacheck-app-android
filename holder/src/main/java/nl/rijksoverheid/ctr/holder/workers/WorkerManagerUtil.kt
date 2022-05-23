package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import nl.rijksoverheid.ctr.holder.workers.ConfigFetchJob
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

    private val interval: Long = if (acc) {
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
            repeatInterval = interval,
            repeatIntervalTimeUnit = intervalUnit)
            .setConstraints(constraints)
            .setInitialDelay(interval, intervalUnit)
            .build()

        println("GIO says schedule worker")
        val workManager = WorkManager.getInstance(context)
        workManager.getWorkInfosByTagLiveData(ConfigFetchJob.uniqueWorkName).observe(lifecycleOwner) {

        }
        WorkManager.getInstance(context).getWorkInfosByTag(ConfigFetchJob.uniqueWorkName).addListener()
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