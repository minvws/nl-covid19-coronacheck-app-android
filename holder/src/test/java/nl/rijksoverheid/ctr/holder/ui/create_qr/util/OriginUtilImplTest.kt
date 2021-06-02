package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class OriginUtilImplTest {

    @Test
    fun `getOriginStates returns correct states`() {
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

        val originEntity3 = OriginEntity(
            id = 0,
            greenCardId = 1,
            type = OriginType.Test,
            eventTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneOffset.UTC),
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(49), ZoneOffset.UTC)
        )

        val validOrigins = originUtil.getOriginState(
            origins = listOf(originEntity1, originEntity2, originEntity3)
        )

        assertEquals(validOrigins[0], OriginState.Valid(originEntity1))
        assertEquals(validOrigins[1], OriginState.Future(originEntity2))
        assertEquals(validOrigins[2], OriginState.Expired(originEntity3))
    }

    @Test
    fun `given a future launch date isActiveInEu returns false`() {
        val clockFromThePast = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clockFromThePast)

        val isActive = originUtil.isActiveInEu("2021-07-01")

        assertFalse(isActive)
    }

    @Test
    fun `given a past launch date isActiveInEu returns true`() {
        val clockFromTheFuture = Clock.fixed(Instant.ofEpochSecond(53 * 12 * 30 * 24 * 60 * 60), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clockFromTheFuture)

        val isActive = originUtil.isActiveInEu("2021-07-01")

        assertTrue(isActive)
    }
}