package nl.rijksoverheid.ctr.design.menu.about

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(AndroidJUnit4::class)
class AboutThisAppFragmentScreenshotTest : ScreenshotTest {
    @Test
    fun aboutThisAppFragment_Screenshot() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fragmentScenario = launchFragmentInContainer<AboutThisAppFragment>(
            bundleOf("data" to AboutThisAppData(
                versionCode = "1000",
                versionName = "4.4-acc",
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
                            ),
                            AboutThisAppData.Destination(
                                text = context.getString(R.string.holder_menu_storedEvents),
                                destinationId = AboutThisAppFragmentDirections.actionSavedEvents().actionId
                            ),
                            AboutThisAppData.ClearAppData(
                                text = context.getString(R.string.holder_menu_resetApp)
                            )
                        )
                    )
                ),
                configVersionHash = "configVersionHash",
                configVersionTimestamp = 1655222474
            )),
            themeResId = R.style.TestAppTheme
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}
