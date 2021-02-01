package nl.rijksoverheid.ctr.shared.repositories

import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.models.Issuers
import nl.rijksoverheid.ctr.shared.models.RemoteAgent
import nl.rijksoverheid.ctr.shared.models.RemoteEvent
import nl.rijksoverheid.ctr.shared.models.TestProofsResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class EventRepository(private val apiClient: TestApiClient) {

    suspend fun issuers(): Issuers {
        return apiClient.getIssuers()
    }

    suspend fun testProofs(accessToken: String, sToken: String, icm: String): TestProofsResult {
        return apiClient.getTestProofs(
            accessToken = accessToken,
            sToken = sToken,
            icm = icm
        )
    }

    suspend fun remoteEvent(id: String): RemoteEvent {
        return apiClient.getEvent(id = id)
    }

    suspend fun remoteAgent(id: String): RemoteAgent {
        return apiClient.getAgent(id = id)
    }
}
