package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteUnomi

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface EventProviderRepository {
    suspend fun unomi(
        url: String,
        token: String
    ): RemoteUnomi

    suspend fun event(
        url: String,
        token: String
    ): RemoteEvents
}

class EventProviderRepositoryImpl(
    private val testProviderApiClient: TestProviderApiClient
) : EventProviderRepository {

    override suspend fun unomi(url: String, token: String): RemoteUnomi {
        return testProviderApiClient
            .unomi(
                url = url,
                authorization = "Bearer $token",
            )
    }

    override suspend fun event(url: String, token: String): RemoteEvents {
        return testProviderApiClient.events(
            url = url,
            authorization = "Bearer $token"
        )
    }
}
