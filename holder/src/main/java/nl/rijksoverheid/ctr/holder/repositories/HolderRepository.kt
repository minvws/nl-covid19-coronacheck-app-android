package nl.rijksoverheid.ctr.holder.repositories

import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.models.RemoteTestProviders

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderRepository(private val api: TestApiClient) {

    suspend fun testProviders(): RemoteTestProviders {
        return api.getConfigCtp()
    }

    suspend fun testIsmJson(accessToken: String, sToken: String, icm: String): String {
        return api.getTestIsm(
            accessToken = accessToken,
            sToken = sToken,
            icm = icm
        ).body()!!.string()
    }

    suspend fun remoteNonce(): RemoteNonce {
        return api.getNonce()
    }
}
