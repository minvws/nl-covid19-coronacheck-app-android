package nl.rijksoverheid.ctr.design.ext

import android.text.format.DateFormat
import java.time.LocalDate
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
fun LocalDate.formatDayMonthYear(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale("nl"),
            "d MMMM yyyy"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)

fun LocalDate.formatDayMonthTime(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "d MMMM HH:mm"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)

fun LocalDate.formatDayShortMonthYear(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "d MMM yyyy"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this).replace(".", "")

fun LocalDate.formatDayMonthYearNumerical(): String =
    DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(
            Locale("nl"),
            "dd-MM-yyyy"
        )
    ).withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)

fun LocalDate.formatMonth(): String =
    DateTimeFormatter.ofPattern("MMMM")
        .withLocale(Locale.getDefault()).withZone(ZoneId.of("CET")).format(this)
