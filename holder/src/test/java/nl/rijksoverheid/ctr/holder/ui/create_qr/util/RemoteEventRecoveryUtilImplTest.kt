package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventRecovery
import org.junit.Test
import java.time.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteEventRecoveryUtilImplTest {

    @Test
    fun `isExpired returns true if sampleDate of event is 180 days ago`() {
        val clockInstant = Instant.parse("2020-01-01T00:00:00.00Z")
        val clock = Clock.fixed(clockInstant, ZoneId.of("UTC"))
        val sampleDate = clockInstant.atZone(ZoneId.of("UTC")).toLocalDate().minusDays(180)

        val remoteEvent = RemoteEventRecovery(
            type = null,
            unique = "",
            isSpecimen = false,
            recovery = RemoteEventRecovery.Recovery(
                sampleDate = sampleDate,
                validFrom = null,
                validUntil = null
            )
        )

        val util = RemoteEventRecoveryUtilImpl(clock)
        assertTrue { util.isExpired(remoteEvent) }
    }

    @Test
    fun `isExpired returns true if sampleDate of event is more than 180 days ago`() {
        val clockInstant = Instant.parse("2020-01-01T00:00:00.00Z")
        val clock = Clock.fixed(clockInstant, ZoneId.of("UTC"))
        val sampleDate = clockInstant.atZone(ZoneId.of("UTC")).toLocalDate().minusDays(181)

        val remoteEvent = RemoteEventRecovery(
            type = null,
            unique = "",
            isSpecimen = false,
            recovery = RemoteEventRecovery.Recovery(
                sampleDate = sampleDate,
                validFrom = null,
                validUntil = null
            )
        )

        val util = RemoteEventRecoveryUtilImpl(clock)
        assertTrue { util.isExpired(remoteEvent) }
    }

    @Test
    fun `isExpired returns false if sampleDate of event is less than 180 days ago`() {
        val clockInstant = Instant.parse("2020-01-01T00:00:00.00Z")
        val clock = Clock.fixed(clockInstant, ZoneId.of("UTC"))
        val sampleDate = clockInstant.atZone(ZoneId.of("UTC")).toLocalDate().minusDays(179)

        val remoteEvent = RemoteEventRecovery(
            type = null,
            unique = "",
            isSpecimen = false,
            recovery = RemoteEventRecovery.Recovery(
                sampleDate = sampleDate,
                validFrom = null,
                validUntil = null
            )
        )

        val util = RemoteEventRecoveryUtilImpl(clock)
        assertFalse { util.isExpired(remoteEvent) }
    }
}