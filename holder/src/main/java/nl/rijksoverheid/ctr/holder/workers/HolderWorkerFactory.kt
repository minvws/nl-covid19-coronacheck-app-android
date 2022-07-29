package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderWorkerFactory(
    private val configResultUseCase: ConfigResultUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            ConfigFetchWorker::class.java.name -> ConfigFetchWorker(
                appContext,
                workerParameters,
                configResultUseCase
            )
            CredentialRefreshWorker::class.java.name -> CredentialRefreshWorker(
                appContext,
                workerParameters,
                configResultUseCase,
                holderDatabaseSyncer
            )
            else -> null
        }
    }
}
