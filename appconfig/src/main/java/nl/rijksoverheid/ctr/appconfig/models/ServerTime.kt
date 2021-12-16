package nl.rijksoverheid.ctr.appconfig.models

import android.os.SystemClock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ServerTime {
    object NotAvailable: ServerTime()
    data class Available(
        val serverTimeMillis: Long,
        val localTimeMillis: Long,
        val uptime: Long = SystemClock.elapsedRealtime()
    ): ServerTime()
}