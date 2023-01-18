package nl.rijksoverheid.ctr.appconfig.models

import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ConfigResult {
    data class Success(val appConfig: String, val publicKeys: String) : ConfigResult()
    data class Error(val error: NetworkRequestResult.Failed) : ConfigResult()
}
