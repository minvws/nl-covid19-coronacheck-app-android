package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderWorkerFactory(
    private val greenCardsUseCase: GreenCardsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RefreshCredentialsJob::class.java.name -> RefreshCredentialsJob(appContext, workerParameters, holderDatabaseSyncer, greenCardsUseCase)
            else -> null
        }
    }
}

