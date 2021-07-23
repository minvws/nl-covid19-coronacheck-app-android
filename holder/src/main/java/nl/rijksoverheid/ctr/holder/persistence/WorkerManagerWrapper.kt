package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardRefreshUtil
import java.util.concurrent.TimeUnit

interface WorkerManagerWrapper {
    fun scheduleNextCredentialsRefreshIfAny()
    fun cancel(context: Context)
}

class WorkerManagerWrapperImpl(
    private val context: Context,
    private val greenCardRefreshUtil: GreenCardRefreshUtil): WorkerManagerWrapper {
    override fun scheduleNextCredentialsRefreshIfAny() {
        GlobalScope.launch {
            val credentialsExpireInDays = greenCardRefreshUtil.credentialsExpireInDays()

            if (credentialsExpireInDays > 0) {
                val request = OneTimeWorkRequestBuilder<RefreshCredentialsJob>()
                    .setInitialDelay(credentialsExpireInDays, TimeUnit.DAYS)
                    .setConstraints(
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    ).build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork("refresh_credentials", ExistingWorkPolicy.REPLACE, request)
            }
        }
    }

    override fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("refresh_credentials")
    }
}