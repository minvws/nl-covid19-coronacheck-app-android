package nl.rijksoverheid.ctr.appconfig.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.appconfig.model.ConfigResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AppStatusUseCase {
    suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus
}

class AppStatusUseCaseImpl : AppStatusUseCase {
    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    val appConfig = config.appConfig
                    when {
                        appConfig.appDeactivated -> AppStatus.Deactivated(appConfig.informationURL)
                        currentVersionCode < appConfig.minimumVersion -> AppStatus.UpdateRequired
                        else -> AppStatus.NoActionRequired
                    }
                }
                is ConfigResult.NetworkError -> {
                    AppStatus.InternetRequired
                }
                is ConfigResult.ServerError -> {
                    AppStatus.InternetRequired
                }
            }
        }
}
