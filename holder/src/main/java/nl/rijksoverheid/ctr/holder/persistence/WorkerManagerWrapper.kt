package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
import java.util.concurrent.TimeUnit

interface WorkerManagerWrapper {
    fun scheduleNextCredentialsRefreshIfAny()
}

class WorkerManagerWrapperImpl(
    private val context: Context,
    private val greenCardsUseCase: GreenCardsUseCase,): WorkerManagerWrapper {
    override fun scheduleNextCredentialsRefreshIfAny() {
        GlobalScope.launch {
            val lastExpiringCard = greenCardsUseCase.lastExpiringCard()

            if (lastExpiringCard is GreenCard.Expiring) {
                val request = OneTimeWorkRequestBuilder<RefreshCredentialsJob>()
                    .setInitialDelay(lastExpiringCard.refreshInDays, TimeUnit.DAYS)
                    .setConstraints(
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    ).build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork("refresh_credentials", ExistingWorkPolicy.REPLACE, request)
            }
        }
    }
}