package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderWorkerFactory(
    private val configResultUseCase: ConfigResultUseCase,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        println("WM-GIO says Create worker: $workerClassName")
        return when (workerClassName) {
            ConfigFetchWorker::class.java.name -> ConfigFetchWorker(
                appContext,
                workerParameters,
                configResultUseCase
            )
            else -> null
        }
    }
}
