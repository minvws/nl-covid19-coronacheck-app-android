package nl.rijksoverheid.ctr.verifier.ui.scanlog.items.util

import android.content.Context
import nl.rijksoverheid.ctr.design.ext.formatTime
import nl.rijksoverheid.ctr.verifier.R
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScanLogListAdapterItemUtil {
    fun getTimeString(context: Context, from: OffsetDateTime, to: OffsetDateTime, isFirstItem: Boolean): String
    fun getAmountString(context: Context, count: Int): String
}

class ScanLogListAdapterItemUtilImpl: ScanLogListAdapterItemUtil {
    override fun getTimeString(
        context: Context,
        from: OffsetDateTime,
        to: OffsetDateTime,
        isFirstItem: Boolean
    ): String {
        return if (isFirstItem) {
            "${from.formatTime(context)} - ${context.getString(R.string.scan_log_list_now)}"
        } else {
            "${from.formatTime(context)} - ${to.formatTime(context)}"
        }
    }

    override fun getAmountString(context: Context, count: Int): String {
        val low = 1.coerceAtLeast(count - (count % 10))
        val high = count + 10 - (count % 10)
        return context.getString(R.string.scan_log_list_entry, low , high)
    }
}