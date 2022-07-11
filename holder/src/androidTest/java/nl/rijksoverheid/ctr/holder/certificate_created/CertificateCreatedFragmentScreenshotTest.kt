package nl.rijksoverheid.ctr.holder.certificate_created

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CertificateCreatedFragmentScreenshotTest: ScreenshotTest {
    @Test
    fun certificateCreatedFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<CertificateCreatedFragment>(
            bundleOf(
                "toolbarTitle" to "Certificate created",
                "title" to "Vaccination and recovery certificates created",
                "description" to "A Dutch vaccination certificate has been created.<br><br>Your positive test result was suitable for creating a recovery certificate as well."
            ),
            themeResId = R.style.TestAppTheme
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}