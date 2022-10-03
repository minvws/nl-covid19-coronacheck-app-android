package nl.rijksoverheid.ctr.holder.utils

import android.app.Application
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearNumerical
import nl.rijksoverheid.ctr.holder.R

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
interface LocalDateUtil {
    fun dateAndDaysSince(localDate: String): Pair<String, String>
}

class LocalDateUtilImpl(
    private val clock: Clock,
    private val application: Application
) : LocalDateUtil {
    override fun dateAndDaysSince(localDate: String): Pair<String, String> {
        return try {
            val parsedLocalDate = LocalDate.parse(localDate, DateTimeFormatter.ISO_DATE)
            val days = LocalDate.now(clock).toEpochDay() - parsedLocalDate.toEpochDay()
            Pair(
                parsedLocalDate.formatDayMonthYearNumerical(),
                "$days ${
                    application.resources.getQuantityString(
                        R.plurals.general_days,
                        days.toInt()
                    )
                }"
            )
        } catch (e: Exception) {
            Pair("", "")
        }
    }
}
