/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

interface AppStatusStringProvider {

    fun getAppStatusStrings(): AppStatusStrings

    data class AppStatusStrings(
        val appStatusDeactivatedTitle: Int,
        val appStatusDeactivatedMessage: Int,
        val appStatusDeactivatedAction: Int,
        val appStatusUpdateRequiredTitle: Int,
        val appStatusUpdateRequiredMessage: Int,
        val appStatusUpdateRequiredAction: Int,
        val appStatusInternetRequiredTitle: Int,
        val appStatusInternetRequiredMessage: Int,
        val appStatusInternetRequiredAction: Int
    )
}
