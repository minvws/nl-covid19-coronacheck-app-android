/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.menu

import android.content.Context
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R

interface AboutThisAppDataModel {
    fun get(context: Context): AboutThisAppData
}

class AboutThisAppDataModelImpl : AboutThisAppDataModel {
    override fun get(context: Context) = AboutThisAppData(
        deeplinkScannerUrl = BuildConfig.DEEPLINK_SCANNER_TEST_URL,
        sections = listOf(
            AboutThisAppData.AboutThisAppSection(
                header = R.string.about_this_app_read_more,
                items = mutableListOf(
                    AboutThisAppData.Url(
                        text = context.getString(R.string.privacy_statement),
                        url = context.getString(R.string.url_privacy_statement)
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_accessibility),
                        url = context.getString(R.string.url_accessibility)
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_colofon),
                        url = context.getString(R.string.about_this_app_colofon_url)
                    )
                )
            )
        )
    )
}
