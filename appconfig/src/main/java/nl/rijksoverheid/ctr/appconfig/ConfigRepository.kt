/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig

import timber.log.Timber

class ConfigRepository(private val api: AppConfigApi) {
    suspend fun getConfigOrDefault(): AppConfig {
        return try {
            api.getConfig()
        } catch (ex: Exception) {
            Timber.w(ex, "Error fetching app config, returning default")
            AppConfig()
        }
    }
}
