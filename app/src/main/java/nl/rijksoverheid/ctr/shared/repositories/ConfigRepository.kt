package nl.rijksoverheid.ctr.shared.repositories

import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.models.Config
import nl.rijksoverheid.ctr.shared.models.ConfigType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class ConfigRepository(private val api: TestApiClient) {

    open suspend fun config(type: ConfigType): Config {
        return when (type) {
            is ConfigType.Holder -> api.getHolderConfig()
            is ConfigType.Verifier -> api.getVerifierConfig()
        }
    }
}
