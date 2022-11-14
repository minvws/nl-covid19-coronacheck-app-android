package nl.rijksoverheid.ctr.holder.fuzzy_matching

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventStringUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.getApplication
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class SelectionDataUtilImplTest : AutoCloseKoinTest() {

    private val codes = listOf(
        AppConfig.Code("code1", "name1"),
        AppConfig.Code("code2", "name2")
    )

    private val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>() {
        every { getCachedAppConfig().providers } returns codes
    }

    private val yourEventsFragmentUtil: YourEventsFragmentUtil by inject()
    private val remoteEventStringUtil: RemoteEventStringUtil by inject()

    private fun getFormattedString(stringResId: Int, formattedString: String): String {
        return getApplication().getString(stringResId, formattedString)
    }
    private val selectionDataUtil by lazy {
        SelectionDataUtilImpl(
            cachedAppConfigUseCase,
            yourEventsFragmentUtil,
            remoteEventStringUtil,
            getApplication().resources::getQuantityString,
            ::getFormattedString,
            getApplication()::getString
        )
    }

    private val remoteEvents = listOf(
        RemoteEventNegativeTest(RemoteEvent.TYPE_NEGATIVE_TEST, "", false, mockk {
            every { sampleDate } returns OffsetDateTime.now()
        }),
        RemoteEventVaccinationAssessment(RemoteEvent.TYPE_VACCINATION_ASSESSMENT, null, mockk {
            every { assessmentDate } returns OffsetDateTime.now()
        }),
        RemoteEventVaccination(RemoteEvent.TYPE_VACCINATION, "", mockk {
            every { date } returns LocalDate.now()
        }),
        RemoteEventVaccination(RemoteEvent.TYPE_VACCINATION, "", mockk {
            every { date } returns LocalDate.now()
        }),
        RemoteEventPositiveTest(RemoteEvent.TYPE_POSITIVE_TEST, "", false, mockk {
            every { sampleDate } returns OffsetDateTime.now()
        }),
        RemoteEventNegativeTest(RemoteEvent.TYPE_NEGATIVE_TEST, "", false, mockk {
            every { sampleDate } returns OffsetDateTime.now()
        }),
        RemoteEventRecovery(RemoteEvent.TYPE_RECOVERY, "", false, mockk {
            every { sampleDate } returns LocalDate.now()
        }),
        RemoteEventVaccination(RemoteEvent.TYPE_VACCINATION, "", mockk {
            every { date } returns LocalDate.now()
        })
    )

    @Test
    fun `events string`() {
        val actual = selectionDataUtil.events(remoteEvents)
        val expected = "3 vaccinaties en 4 testuitslagen en 1 vaccinatiebeoordeling"
        assertEquals(expected, actual)
    }

    @Test
    fun `details string`() {
        val actualData = selectionDataUtil.details("code1", remoteEvents)
        val expectedData = listOf(
            "Negatieve testuitslag",
            "Bezoekersbewijs",
            "Vaccinatie",
            "Vaccinatie",
            "Positieve testuitslag",
            "Negatieve testuitslag",
            "Herstelbewijs",
            "Vaccinatie"
        )

        actualData.forEachIndexed { index, data ->
            assertEquals(expectedData[index], data.type)
        }
    }

    @Test
    fun `details string for dcc vaccinations display dose`() {
        val actualData = selectionDataUtil.details("dcc_code1", listOf(RemoteEventVaccination(RemoteEvent.TYPE_VACCINATION, "", mockk {
            every { date } returns LocalDate.now()
            every { doseNumber } returns "2"
            every { totalDoses } returns "2"
        })))

        assertEquals("Vaccinatie dosis 2/2", actualData.first().type)
    }
}
