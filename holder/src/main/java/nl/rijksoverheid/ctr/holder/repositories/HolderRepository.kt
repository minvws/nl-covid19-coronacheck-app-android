package nl.rijksoverheid.ctr.holder.repositories

import nl.rijksoverheid.ctr.shared.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.api.TestApiClient

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderRepository(private val api: TestApiClient) {

    suspend fun remoteNonce(): RemoteNonce {
        return api.getNonce()
    }
}
