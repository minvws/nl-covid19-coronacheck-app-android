package nl.rijksoverheid.ctr.data.api

import nl.rijksoverheid.ctr.data.models.Issuers
import nl.rijksoverheid.ctr.data.models.TestResults
import retrofit2.http.GET
import retrofit2.http.Query

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestApiClient {

    @GET("/citizen/get_public_keys/")
    suspend fun getIssuers(): Issuers

    @GET("/citizen/get_test_results/")
    suspend fun getTestResults(@Query("userUUID") userUUID: String): TestResults
}
