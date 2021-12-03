package nl.rijksoverheid.ctr.verifier.ui.scanlog.items.util

import android.content.Context
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.verifier.R
import java.time.Clock
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

interface ScanLogFirstInstallTimeAdapterItemUtil {
    fun getFirstInstallTimeString(context: Context, firstInstallTime: OffsetDateTime): String
}

class ScanLogFirstInstallTimeAdapterItemUtilImpl(private val clock: Clock): ScanLogFirstInstallTimeAdapterItemUtil {
    override fun getFirstInstallTimeString(context: Context, firstInstallTime: OffsetDateTime): String {
        val daysAgo = TimeUnit.SECONDS.toDays(OffsetDateTime.now(clock).toEpochSecond() - firstInstallTime.toEpochSecond())
        val moreThanMonthAgo = daysAgo >= 30
        return if (moreThanMonthAgo) {
            context.getString(R.string.scan_log_footer_long_time)
        } else {
            context.getString(R.string.scan_log_footer_in_use, firstInstallTime.formatDayMonthTime(context))
        }
    }
}