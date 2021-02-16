/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.cachestrategy.CacheStrategy
import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ConfigRepositoryTest {
    @Test
    fun `ConfigRepository returns app config`() = runBlocking {
        val config = AppConfig(minimumVersion = 2, message = "test message", playStoreURL = "dummy")
        val api = object : AppConfigApi {
            override suspend fun getConfig(cacheStrategy: CacheStrategy): AppConfig = config
        }

        val repository = ConfigRepository(api)

        assertEquals(config, repository.getConfigOrDefault())
    }

    @Test
    fun `ConfigRepository returns default app config when an error occurs`() = runBlocking {
        val api = object : AppConfigApi {
            override suspend fun getConfig(cacheStrategy: CacheStrategy): AppConfig =
                throw IOException("network error")
        }

        val repository = ConfigRepository(api)

        assertEquals(AppConfig(), repository.getConfigOrDefault())
    }
}