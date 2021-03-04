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
        val appStatusDeactivatedTitle: Int = 0,
        val appStatusDeactivatedAction: Int = 0,
        val appStatusUpdateRequiredTitle: Int = 0,
        val appStatusUpdateRequiredMessage: Int = 0,
        val appStatusUpdateRequiredAction: Int = 0,
    )
}