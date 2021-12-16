package nl.rijksoverheid.ctr.appconfig.models

import android.os.SystemClock


sealed class ServerTime {
    object NotAvailable: ServerTime()
    data class Available(
        val serverTimeMillis: Long,
        val localTimeMillis: Long,
        val uptime: Long = SystemClock.elapsedRealtime()
    ): ServerTime()
}