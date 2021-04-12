/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design.menu.about

import androidx.annotation.StringRes

interface AboutAppResourceProvider {

    fun getAboutThisAppData(): AboutData

    data class AboutData(
        @StringRes val aboutThisAppTextResource: Int,
        @StringRes val appVersionTextResource: Int,
        val appVersionName: String,
        val appVersionCode: String
    )
}