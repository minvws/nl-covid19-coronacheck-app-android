package nl.rijksoverheid.ctr.design.ext

import android.content.Context
import android.text.format.DateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun OffsetDateTime.formatDateTime(context: Context): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            if (DateFormat.is24HourFormat(context)) "EEEE d MMMM HH:mm" else "EEEE d MMMM hh:mm"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)

fun OffsetDateTime.formatDayMonthTime(context: Context): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            if (DateFormat.is24HourFormat(context)) "d MMMM HH:mm" else "d MMMM hh:mm"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)

fun OffsetDateTime.formatDayMonthYearTimeNumerical(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "dd-MM-yyyy HH:mm"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)
