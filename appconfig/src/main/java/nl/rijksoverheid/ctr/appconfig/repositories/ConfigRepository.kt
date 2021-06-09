/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.repositories

import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import okio.BufferedSource
import java.io.InputStream

interface ConfigRepository {
    suspend fun getConfig(): AppConfig
    suspend fun getPublicKeys(): BufferedSource
}

class ConfigRepositoryImpl(private val api: AppConfigApi) : ConfigRepository {
    override suspend fun getConfig(): AppConfig {
        return api.getConfig()
    }

    override suspend fun getPublicKeys(): BufferedSource {
        return api.getPublicKeys().source()
    }
}
