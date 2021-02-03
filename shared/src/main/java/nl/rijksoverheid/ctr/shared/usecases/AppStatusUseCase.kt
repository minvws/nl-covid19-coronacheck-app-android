package nl.rijksoverheid.ctr.shared.usecases

import nl.rijksoverheid.ctr.shared.models.AppStatus
import nl.rijksoverheid.ctr.shared.models.Config
import nl.rijksoverheid.ctr.shared.models.ConfigType
import nl.rijksoverheid.ctr.shared.repositories.ConfigRepository

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppStatusUseCase(private val configRepository: ConfigRepository) {

    suspend fun status(currentVersionCode: Int, type: ConfigType): AppStatus {
        val config = configRepository.config(type = type)
        return when {
            appDeactivated(config = config) -> AppStatus.AppDeactivated
            !upToDate(
                currentVersionCode = currentVersionCode,
                config = config
            ) -> AppStatus.ShouldUpdate
            else -> AppStatus.Ok
        }
    }

    private fun upToDate(currentVersionCode: Int, config: Config): Boolean {
        return currentVersionCode >= config.minimumVersion
    }

    private fun appDeactivated(config: Config): Boolean {
        return config.appDeactivated
    }
}
