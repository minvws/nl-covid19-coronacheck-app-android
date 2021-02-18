/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.model

sealed class AppStatus {
    data class UpdateRequired(val message: String?) : AppStatus()
    data class Deactivated(val message: String, val informationUrl: String) : AppStatus()
    object UpToDate : AppStatus()
}
