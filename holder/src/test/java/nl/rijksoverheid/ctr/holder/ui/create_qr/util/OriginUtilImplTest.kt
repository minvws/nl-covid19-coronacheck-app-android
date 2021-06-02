package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.*

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
    fun `given a future launch date activeAt can be formatted to 3 June`() {
        val clockFromThePast = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val originUtil = OriginUtilImpl(clockFromThePast)

        val date = originUtil.activeAt("2021-06-03T14:00:00+00:00")

        val locale = Locale.US
        assertEquals("3 June", "${date.dayOfMonth} ${date.month.toString().toLowerCase(locale).capitalize(locale)}")
    }
}
