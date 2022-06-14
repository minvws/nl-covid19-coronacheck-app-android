package nl.rijksoverheid.ctr.holder.your_events

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class YourEventsFragmentTest: ScreenshotTest {
    @Test
    fun yourEventsFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<YourEventsFragment>(
            bundleOf(
                "toolbarTitle" to "Retrieved vaccinations",
                "type" to YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = mapOf(
                        RemoteProtocol(
                            providerIdentifier = "ZZZ",
                            protocolVersion = "3.0",
                            status = RemoteProtocol.Status.COMPLETE,
                            holder = RemoteProtocol.Holder(
                                infix = "van",
                                firstName = "Corrie",
                                lastName = "Geer",
                                birthDate = "1960-01-01"
                            ),
                            events = listOf(
                                RemoteEventVaccination(
                                    type = "vaccination",
                                    unique = "3ca0c918-5a20-4033-8fc3-8334cd5c63af",
                                    vaccination = RemoteEventVaccination.Vaccination(
                                        date = LocalDate.parse("2022-03-16"),
                                        hpkCode = "2924528",
                                        type = "",
                                        brand = "",
                                        completedByMedicalStatement = false,
                                        completedByPersonalStatement = false,
                                        completionReason = null,
                                        doseNumber = null,
                                        totalDoses = null,
                                        manufacturer = "",
                                        country = "NL"
                                    )
                                ),
                                RemoteEventVaccination(
                                    type = "vaccination",
                                    unique = "c9e2f21b-50c9-407d-9ef9-53e534ad6aa2",
                                    vaccination = RemoteEventVaccination.Vaccination(
                                        date = LocalDate.parse("2022-02-14"),
                                        hpkCode = "2924528",
                                        type = "",
                                        brand = "",
                                        completedByMedicalStatement = false,
                                        completedByPersonalStatement = false,
                                        completionReason = null,
                                        doseNumber = null,
                                        totalDoses = null,
                                        manufacturer = "",
                                        country = "NL"
                                    )
                                )
                            )
                        ) to "".toByteArray()
                    ),
                    eventProviders = listOf(EventProvider("ZZZ", "TEST")),
                ),
                "flow" to HolderFlow.Vaccination
             ),
            themeResId = R.style.TestAppTheme
        )
        compareScreenshot(fragmentScenario.waitForFragment())
    }
}