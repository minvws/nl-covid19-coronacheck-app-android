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
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys

interface ConfigRepository {
    suspend fun getConfig(): AppConfig
    suspend fun getPublicKeys(): PublicKeys
}

class ConfigRepositoryImpl(private val api: AppConfigApi): ConfigRepository {
    override suspend fun getConfig(): AppConfig {
        return api.getConfig()
    }

    override suspend fun getPublicKeys(): PublicKeys {
        return api.getPublicKeys()
    }
}
