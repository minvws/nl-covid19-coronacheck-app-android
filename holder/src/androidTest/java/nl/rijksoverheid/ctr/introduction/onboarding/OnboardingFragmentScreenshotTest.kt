package nl.rijksoverheid.ctr.introduction.onboarding

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
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
class OnboardingFragmentScreenshotTest : ScreenshotTest {

    @Test
    fun onboardingFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<OnboardingFragment>(
            bundleOf("introduction_data" to IntroductionData(
                listOf(
                    OnboardingItem(
                        R.drawable.illustration_onboarding_1,
                        R.string.onboarding_screen_1_title,
                        R.string.onboarding_screen_1_description
                    )
                )
            )
            ),
            themeResId = R.style.TestAppTheme
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}
