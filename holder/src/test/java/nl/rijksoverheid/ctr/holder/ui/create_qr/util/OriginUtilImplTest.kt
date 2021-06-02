package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class OriginUtilImplTest {

    @Test
    fun `getValidOrigins returns origin that are in time window`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val originEntity1 = OriginEntity(
            id = 0,
            greenCardId = 1,
            type = OriginType.Test,
            eventTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneOffset.UTC),
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(10), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(100), ZoneOffset.UTC)
        )

        val originEntity2 = OriginEntity(
            id = 0,
            greenCardId = 1,
            type = OriginType.Test,
            eventTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneOffset.UTC),
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(100), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(200), ZoneOffset.UTC)
        )

        val validOrigins = originUtil.getValidOrigins(
            origins = listOf(originEntity1, originEntity2)
        )

        assertEquals(listOf(originEntity1), validOrigins)
    }

    @Test
    fun `given a future launch date isActiveInEu returns false`() {
        val clockFromThePast = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clockFromThePast)

        val isActive = originUtil.isActiveInEu("2021-06-03T14:00:00+00:00")

        assertFalse(isActive)
    }

    @Test
    fun `given a past launch date isActiveInEu returns true`() {
        val clockFromTheFuture = Clock.fixed(Instant.ofEpochSecond(53 * 12 * 30 * 24 * 60 * 60), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clockFromTheFuture)

        val isActive = originUtil.isActiveInEu("2021-06-03T14:00:00+00:00")

        assertTrue(isActive)
    }

    @Test
    fun `given a future launch date daysSinceActive returns days till then`() {
        val clockFromThePast = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clockFromThePast)

        val daysLeft = originUtil.daysSinceActive("2021-06-03T14:00:00+00:00")

        assertEquals(18782, daysLeft)
    }
}
