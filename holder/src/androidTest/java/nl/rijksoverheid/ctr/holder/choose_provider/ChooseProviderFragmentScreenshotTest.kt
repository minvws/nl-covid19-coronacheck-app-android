package nl.rijksoverheid.ctr.holder.choose_provider

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseProviderFragmentTest: ScreenshotTest {
    @Test
    fun chooseProviderFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<ChooseProviderFragment>(
            themeResId = R.style.TestAppTheme
        )
        compareScreenshot(fragmentScenario.waitForFragment())
    }
}