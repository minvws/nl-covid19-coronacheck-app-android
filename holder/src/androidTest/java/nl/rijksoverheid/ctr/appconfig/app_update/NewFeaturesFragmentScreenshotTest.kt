package nl.rijksoverheid.ctr.appconfig.app_update

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
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
class NewFeaturesFragmentScreenshotTest : ScreenshotTest {
    @Test
    fun newFeaturesFragment_TwoPages_Screenshot() {
        val fragmentScenario = launchFragment(pagesSize = 2)

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun newFeaturesFragment_OnePage_Screenshot() {
        val fragmentScenario = launchFragment(pagesSize = 1)

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    private fun launchFragment(pagesSize: Int): FragmentScenario<NewFeaturesFragment> {
        val newFeatures = buildList<NewFeatureItem> {
            for (i in 0 until pagesSize) {
                add(NewFeatureItem(
                    imageResource = R.drawable.ic_paper_proof_international_qr,
                    subTitleColor = R.color.primary_blue,
                    titleResource = R.string.holder_newintheapp_foreignproofs_title,
                    description = R.string.holder_newintheapp_foreignproofs_body
                ))
            }
        }
        return launchFragmentInContainer(
            bundleOf(
                "app_update_data" to AppUpdateData(
                    newFeatures = newFeatures,
                    newTerms = NewTerms(
                        version = 2,
                        needsConsent = false
                    ),
                    newFeatureVersion = 8,
                    hideConsent = true
                )
            ),
            themeResId = R.style.TestAppTheme
        )
    }
}
