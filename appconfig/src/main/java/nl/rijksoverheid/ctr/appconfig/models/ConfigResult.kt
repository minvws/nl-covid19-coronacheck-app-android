package nl.rijksoverheid.ctr.appconfig.models

import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import okio.BufferedSource

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ConfigResult {
    data class Success(val appConfig: String, val publicKeys: String) : ConfigResult()
    object Error : ConfigResult()
}
