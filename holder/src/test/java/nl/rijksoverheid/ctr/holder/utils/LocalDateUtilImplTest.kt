package nl.rijksoverheid.ctr.holder.utils

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class LocalDateUtilImplTest : AutoCloseKoinTest() {

    private val clock = Clock.fixed(Instant.parse("2022-04-25T09:00:00.00Z"), ZoneId.of("UTC"))

    private val localDateUtil =
        LocalDateUtilImpl(clock, ApplicationProvider.getApplicationContext())

    @Test
    fun `a valid date string returns formatted date and days till now`() {
        val date = "2022-01-04"

        val (parsedDate, daysSince) = localDateUtil.dateAndDaysSince(date)

        assertEquals("04-01-2022", parsedDate)
        assertEquals("111 days", daysSince)
    }

    @Test
    fun `an invalid date returns empty strings`() {
        val date = "2022--01-04"

        val (parsedDate, daysSince) = localDateUtil.dateAndDaysSince(date)

        assertEquals("", parsedDate)
        assertEquals("", daysSince)
    }
}