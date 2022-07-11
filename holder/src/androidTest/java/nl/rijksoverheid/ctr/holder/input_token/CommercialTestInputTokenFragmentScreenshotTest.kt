package nl.rijksoverheid.ctr.holder.input_token

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommercialTestInputTokenFragmentScreenshotTest: ScreenshotTest {
    @Test
    fun commercialTestInputTokenFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<CommercialTestInputTokenFragment>(
            bundleOf(
                "token" to "Token"
            ),
            themeResId = R.style.TestAppTheme
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}