/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.model.AppStatus

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppStatusUseCase(private val configRepository: ConfigRepository) {
    suspend fun status(currentVersionCode: Int): AppStatus {
        val config = configRepository.getConfigOrDefault()
        return when {
            config.appDeactivated -> AppStatus.Deactivated
            currentVersionCode < config.minimumVersion -> AppStatus.UpdateRequired
            else -> AppStatus.UpToDate
        }
    }
}
