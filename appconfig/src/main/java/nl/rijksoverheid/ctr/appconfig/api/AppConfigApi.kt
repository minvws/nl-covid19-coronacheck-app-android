/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api

import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import retrofit2.http.GET

interface AppConfigApi {
    @GET("config")
    suspend fun getConfig(): AppConfig
}