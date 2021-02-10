package nl.rijksoverheid.ctr.holder.repositories

import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.models.RemoteTestProviders
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.post.GetTestIsmPostData
import nl.rijksoverheid.ctr.shared.models.post.GetTestResultPostData
import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderRepository(private val api: TestApiClient) {

    suspend fun remoteTestResult(
        url: String,
        token: String,
        verifierCode: String
    ): RemoteTestResult {
        return api.getTestResult(
            url = url,
            authorization = "Bearer $token",
            data = GetTestResultPostData(
                verifierCode
            )
        )
    }

    suspend fun testProviders(): RemoteTestProviders {
        return api.getConfigCtp()
    }

    suspend fun testIsmJson(test: String, sToken: String, icm: String): String {
        return api.getTestIsm(
            GetTestIsmPostData(
                test = JSONObject(test).toString(),
                sToken = sToken,
                icm = JSONObject(icm).toString()
            )
        ).body()!!.string()
    }

    suspend fun remoteNonce(): RemoteNonce {
        return api.getNonce()
    }
}
