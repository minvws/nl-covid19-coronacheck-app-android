package nl.rijksoverheid.ctr.verifier.ui.scanlog.items.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.verifier.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ScanLogListAdapterItemUtilImplTest: AutoCloseKoinTest() {

    @Test
    fun `getTimeString return correct string when first item`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val util = ScanLogListAdapterItemUtilImpl()
        val timeString = util.getTimeString(
            context = context,
            from = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:00.00Z"),
                ZoneId.of("UTC")
            ),
            to = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:10:00.00Z"),
                ZoneId.of("UTC")
            ),
            isFirstItem = true
        )

        val expectedTimeString = "01:00 - ${context.getString(R.string.scan_log_list_now)}"

        assertEquals(expectedTimeString, timeString)
    }

    @Test
    fun `getTimeString return correct string when not first item`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val util = ScanLogListAdapterItemUtilImpl()
        val timeString = util.getTimeString(
            context = context,
            from = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:00.00Z"),
                ZoneId.of("UTC")
            ),
            to = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:10:00.00Z"),
                ZoneId.of("UTC")
            ),
            isFirstItem = false
        )

        val expectedTimeString = "01:00 - 01:10"

        assertEquals(expectedTimeString, timeString)
    }

    @Test
    fun `getAmountString returns correct string when count is 9`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val util = ScanLogListAdapterItemUtilImpl()
        val amountString = util.getAmountString(
            context = context,
            count = 9
        )

        val expectedCountString = context.getString(R.string.scan_log_list_entry, 1, 10)

        assertEquals(expectedCountString, amountString)
    }

    @Test
    fun `getAmountString returns correct string when count is 10`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val util = ScanLogListAdapterItemUtilImpl()
        val amountString = util.getAmountString(
            context = context,
            count = 10
        )

        val expectedCountString = context.getString(R.string.scan_log_list_entry, 10, 20)

        assertEquals(expectedCountString, amountString)
    }
}