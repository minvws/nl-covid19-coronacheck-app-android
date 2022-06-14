package nl.rijksoverheid.ctr.holder.your_events

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreen
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YourEventExplanationFragmentTest: ScreenshotTest {
    @Test
    fun yourEventExplanationFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<YourEventExplanationFragment>(
            bundleOf(
                "data" to arrayOf(
                    InfoScreen(
                        title = "Details",
                        description = "These details about your vaccination were retrieved from MVWS-TEST:<br/><br/>Name: <b>van Geer, Corrie</b><br/>Date of birth: <b>1 January 1960</b><br/><br/>Disease targeted: <b>COVID-19</b><br/>Vaccine: <b>Pfizer (Comirnaty)</b><br/>Vaccine type: <b>SARS-CoV-2 mRNA vaccine</b><br/>Vaccine manufacturer: <b>Biontech Manufacturing GmbH</b><br/>Date of vaccination: <b>16 March 2022</b><br/>Member state of vaccination: <b>Netherlands</b><br/>Unique vaccination identifier: <b>3ca0c918-5a20-4033-8fc3-8334cd5c63af</b><br/>"
                    )
                ),
                "toolbarTitle" to "Details"
            ),
            themeResId = R.style.TestAppTheme,
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}