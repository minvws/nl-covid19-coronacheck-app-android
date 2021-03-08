package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.text.format.DateFormat
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
            if (DateFormat.is24HourFormat(context)) "d MMMM hh:mm" else "d MMMM hh:mm a"
        )
    ).withZone(ZoneId.systemDefault()).format(this)

fun OffsetDateTime.formatDate(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "d MMMM yyyy"
        )
    ).withZone(ZoneId.systemDefault()).format(this)

fun OffsetDateTime.formatDateDayMonth(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "d MMMM"
        )
    ).withZone(ZoneId.systemDefault()).format(this)

fun OffsetDateTime.formatHourMinutes(context: Context): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            if (DateFormat.is24HourFormat(context)) "hh:mm" else "hh:mm a"
        )
    ).withZone(ZoneId.systemDefault()).format(this)

fun OffsetDateTime.formatDateShort(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "dd-MM-yyyy"
        )
    ).withZone(ZoneId.systemDefault()).format(this)
