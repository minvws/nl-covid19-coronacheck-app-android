package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import com.squareup.moshi.Moshi
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class RemoteEventUtilImplTest {

    private val moshi: Moshi = mockk(relaxed = true)
    private val cachedAppConfigUseCase: CachedAppConfigUseCase = mockk(relaxed = true)
    private val clock = Clock.fixed(Instant.parse("2021-06-01T00:00:00.00Z"), ZoneId.of("UTC"))

    @Test
    fun `removeDuplicateEvents removes duplicate vaccination events`() {
        val util = RemoteEventUtilImpl(
            clock = clock,
            moshi = moshi,
            cachedAppConfigUseCase = cachedAppConfigUseCase
        )

        val events = util.removeDuplicateEvents(listOf(vaccination(), vaccination()))
        assertEquals(1, events.size)
    }

    private fun vaccination(
        doseNumber: String = "1",
        totalDoses: String = "1",
        hpkCode: String? = "hpkCode",
        manufacturer: String? = null,
        clock: Clock = Clock.fixed(Instant.parse("2021-06-01T00:00:00.00Z"), ZoneId.of("UTC")),
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
            completionReason = null,
        )
    )
}