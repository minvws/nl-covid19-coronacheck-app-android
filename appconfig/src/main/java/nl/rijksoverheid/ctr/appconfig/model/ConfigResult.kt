package nl.rijksoverheid.ctr.appconfig.model

import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ConfigResult {
    data class Success(val appConfig: AppConfig, val publicKeys: PublicKeys) : ConfigResult()
    object Error : ConfigResult()
}
