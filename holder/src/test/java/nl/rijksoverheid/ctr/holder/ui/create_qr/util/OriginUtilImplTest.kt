package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
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
    fun `hideSubtitle returns false if green card type is Domestic and origin will expire in less than 3 years`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val originState = OriginState.Valid(
            origin = OriginEntity(
                id = 0,
                greenCardId = 0,
                type = OriginType.Test,
                eventTime = OffsetDateTime.now(clock),
                expirationTime = OffsetDateTime.now(clock),
                validFrom = OffsetDateTime.now(clock).plusYears(2)
            )
        )

        assertFalse(
            originUtil.hideSubtitle(
                greenCardType = GreenCardType.Domestic,
                originState = originState
            )
        )
    }

    @Test
    fun `hideSubtitle returns true if green card type is Domestic and origin will expire in 3 years`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val originState = OriginState.Valid(
            origin = OriginEntity(
                id = 0,
                greenCardId = 0,
                type = OriginType.Test,
                eventTime = OffsetDateTime.now(clock),
                expirationTime = OffsetDateTime.now(clock).plusYears(3),
                validFrom = OffsetDateTime.now(clock)
            )
        )

        assertTrue(
            originUtil.hideSubtitle(
                greenCardType = GreenCardType.Domestic,
                originState = originState
            )
        )
    }

    @Test
    fun `hideSubtitle returns false if green card type is Eu and origin will expire in 3 years`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val originState = OriginState.Valid(
            origin = OriginEntity(
                id = 0,
                greenCardId = 0,
                type = OriginType.Test,
                eventTime = OffsetDateTime.now(clock),
                expirationTime = OffsetDateTime.now(clock),
                validFrom = OffsetDateTime.now(clock).plusYears(3)
            ))

        assertFalse(originUtil.hideSubtitle(
            greenCardType = GreenCardType.Eu,
            originState = originState
        ))
    }

    @Test
    fun `when origin is valid before the threshold day from now, it's valid`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val origin = fakeOriginEntity(
            validFrom = OffsetDateTime.now(clock).plusDays(27),
            expirationTime = OffsetDateTime.now(clock)
        )
        originUtil.isValidWithinRenewalThreshold(28L, origin)
    }

    @Test
    fun `when origin is valid on the threshold day from now, it's not valid`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val origin = fakeOriginEntity(
            validFrom = OffsetDateTime.now(clock).plusDays(28),
            expirationTime = OffsetDateTime.now(clock)
        )
        originUtil.isValidWithinRenewalThreshold(28L, origin)
    }

    @Test
    fun `when origin is valid after the threshold day from now, it's not valid`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val origin = fakeOriginEntity(
            validFrom = OffsetDateTime.now(clock).plusDays(29),
            expirationTime = OffsetDateTime.now(clock)
        )
        originUtil.isValidWithinRenewalThreshold(28L, origin)
    }

    @Test
    fun `when origin expires after now, it's valid`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val origin = fakeOriginEntity(
            validFrom = OffsetDateTime.now(clock).plusDays(29),
            expirationTime = OffsetDateTime.now(clock).plusDays(1)
        )
        originUtil.isValidWithinRenewalThreshold(28L, origin)
    }

    @Test
    fun `when origin expires before now, it's not valid`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clock)

        val origin = fakeOriginEntity(
            validFrom = OffsetDateTime.now(clock).plusDays(29),
            expirationTime = OffsetDateTime.now(clock).minusDays(1)
        )
        originUtil.isValidWithinRenewalThreshold(28L, origin)
    }
}
