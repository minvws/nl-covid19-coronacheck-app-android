package nl.rijksoverheid.ctr.your_events.utils

import com.squareup.moshi.Moshi
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteEventUtilImplTest {

    private val moshi: Moshi = mockk(relaxed = true)

    @Test
    fun `removeDuplicateEvents removes duplicate vaccination events`() {
        val util = RemoteEventUtilImpl(
            moshi = moshi
        )

        val events = util.removeDuplicateEvents(listOf(vaccination(), vaccination()))
        assertEquals(1, events.size)
    }

    private fun vaccination(
        doseNumber: String = "1",
        totalDoses: String = "1",
        hpkCode: String? = "hpkCode",
        manufacturer: String? = null,
        clock: Clock = Clock.fixed(Instant.parse("2021-06-01T00:00:00.00Z"), ZoneId.of("UTC"))
    ) = RemoteEventVaccination(
        type = "vaccination",
        unique = null,
        vaccination = RemoteEventVaccination.Vaccination(
            date = LocalDate.now(clock),
            type = "vaccination",
            hpkCode = hpkCode,
            brand = "Brand",
            doseNumber = doseNumber,
            totalDoses = totalDoses,
            manufacturer = manufacturer,
            completedByMedicalStatement = null,
            completedByPersonalStatement = null,
            country = null,
            completionReason = null
        )
    )
}
