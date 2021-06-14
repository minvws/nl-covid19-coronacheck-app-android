package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.work.*
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
import java.util.concurrent.TimeUnit

class RefreshCredentialsJob(
    context: Context,
    params: WorkerParameters,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val greenCardsUseCase: GreenCardsUseCase,
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val expiringCardOriginType = greenCardsUseCase.expiringCardOriginType()
        val syncWithRemote = expiringCardOriginType != null
        if (syncWithRemote) {
            return when (holderDatabaseSyncer.sync(expiringCardOriginType, true)) {
                DatabaseSyncerResult.Success -> Result.success()
                else -> Result.retry()
            }
        }

        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RefreshCredentialsJob>(7L, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("refresh_credentials", ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}

