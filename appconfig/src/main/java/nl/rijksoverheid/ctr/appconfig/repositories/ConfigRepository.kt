/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.repositories

import nl.rijksoverheid.ctr.appconfig.api.AppConfigApi
import nl.rijksoverheid.ctr.appconfig.models.ConfigResponse
import retrofit2.HttpException

interface ConfigRepository {
    suspend fun getConfig(): ConfigResponse
    suspend fun getPublicKeys(): String
}

@Suppress("BlockingMethodInNonBlockingContext")
class ConfigRepositoryImpl(private val api: AppConfigApi) : ConfigRepository {
    override suspend fun getConfig(): ConfigResponse {
        val response = api.getConfig()
        val responseBody = response.body()

        if (!response.isSuccessful || responseBody == null) {
            throw HttpException(response)
        }

        return ConfigResponse(
            body = responseBody.toString(),
            headers = response.headers()
        )
    }

    override suspend fun getPublicKeys(): String {
        return api.getPublicKeys().source().readUtf8()
    }
}
