package nl.rijksoverheid.ctr.appconfig.models

import android.os.SystemClock


sealed class ServerInfo {
    object NotAvailable: ServerInfo()
    data class Available(
        val serverTimeMillis: Long,
        val localTimeMillis: Long,
        val uptime: Long = SystemClock.elapsedRealtime()
    ): ServerInfo()
}