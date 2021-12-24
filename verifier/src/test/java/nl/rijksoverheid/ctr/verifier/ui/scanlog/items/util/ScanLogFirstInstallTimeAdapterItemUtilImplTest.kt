package nl.rijksoverheid.ctr.verifier.ui.scanlog.items.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.verifier.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ScanLogFirstInstallTimeAdapterItemUtilImplTest: AutoCloseKoinTest() {

    @Test
    fun `getFirstInstallTimeString returns correct string when more than 30 days ago`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val util = ScanLogFirstInstallTimeAdapterItemUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-31T00:00:00.00Z"), ZoneId.of("UTC"))
        )

        val firstInstallTimeString = util.getFirstInstallTimeString(
            context = context,
            firstInstallTime = OffsetDateTime.ofInstant(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )

        Assert.assertEquals(
            context.getString(R.string.scan_log_footer_long_time),
            firstInstallTimeString
        )
    }

    @Test
    fun `getFirstInstallTimeString returns correct string when less than 30 days ago`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val util = ScanLogFirstInstallTimeAdapterItemUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-29T00:00:00.00Z"), ZoneId.of("UTC"))
        )

        val firstInstallTime = OffsetDateTime.ofInstant(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val firstInstallTimeString = util.getFirstInstallTimeString(
            context = context,
            firstInstallTime = firstInstallTime
        )

        Assert.assertEquals(
            context.getString(R.string.scan_log_footer_in_use, firstInstallTime.formatDayMonthTime(context)),
            firstInstallTimeString
        )
    }
}