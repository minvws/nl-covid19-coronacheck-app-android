package nl.rijksoverheid.ctr.holder.could_not_create_qr

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CouldNotCreateQrFragmentScreenshotTest: ScreenshotTest {
    @Test
    fun certificateCreatedFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<CouldNotCreateQrFragment>(
            bundleOf(
                "toolbarTitle" to "Retrieve test result",
                "title" to "No negative test result",
                "description" to "No negative test result available.",
                "buttonTitle" to "To my certificates"
            ),
            themeResId = R.style.TestAppTheme
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}
