package nl.rijksoverheid.ctr.shared.api

import nl.rijksoverheid.ctr.citizen.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.models.Issuers
import nl.rijksoverheid.ctr.shared.models.RemoteAgent
import nl.rijksoverheid.ctr.shared.models.RemoteEvent
import nl.rijksoverheid.ctr.shared.models.TestResults
import retrofit2.http.GET
import retrofit2.http.Path
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
    suspend fun getTestResults(@Query("access_token") accessToken: String): TestResults

    @GET("/verifier/get_event/{id}")
    suspend fun getEvent(@Path("id") id: String): RemoteEvent

    @GET("verifier/get_agent/{id}")
    suspend fun getAgent(@Path("id") id: String): RemoteAgent

    @GET("/citizen/get_nonce/")
    suspend fun getNonce(): RemoteNonce
}
