/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaperProofStartFragmentScreenshotTest: ScreenshotTest {

    @Test
    fun PaperProofStartFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<PaperProofStartScanningFragment>(
            themeResId = R.style.TestAppTheme
        )
        compareScreenshot(fragmentScenario.waitForFragment())
    }
}