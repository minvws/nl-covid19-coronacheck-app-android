/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.eu.repositories

import nl.rijksoverheid.ctr.appconfig.eu.api.EuPublicKeysApi
import okhttp3.ResponseBody

interface EuPublicKeysRepository {
    suspend fun getPublicKeys(): ResponseBody
    suspend fun config(): ResponseBody
}

class EuPublicKeysRepositoryImpl(private val api: EuPublicKeysApi) : EuPublicKeysRepository {

    override suspend fun getPublicKeys(): ResponseBody {
        return api.getPublicKeys()
    }

    override suspend fun config(): ResponseBody {
        return api.config()
    }
}