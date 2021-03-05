/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecase

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.ConfigRepository
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import java.io.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppConfigUseCase(
    private val configRepository: ConfigRepository,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) {

    suspend fun config(currentVersionCode: Int): AppStatus {
        return try {
            val config = configRepository.getConfig()
            cachedAppConfigUseCase.persistAppConfig(config)
            val publicKeys = configRepository.getPublicKeys()
            cachedAppConfigUseCase.persistPublicKeys(publicKeys)
            return when {
                config.appDeactivated -> AppStatus.Deactivated(config.informationURL)
                currentVersionCode < config.minimumVersion -> AppStatus.UpdateRequired
                else -> AppStatus.NoActionRequired
            }
        } catch (e: IOException) {
            AppStatus.InternetRequired
        }
    }
}
