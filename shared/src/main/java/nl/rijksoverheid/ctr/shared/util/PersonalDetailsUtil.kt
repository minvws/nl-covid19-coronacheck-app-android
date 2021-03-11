package nl.rijksoverheid.ctr.shared.util

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface PersonalDetailsUtil {
    fun getPersonalDetails(
        firstNameInitial: String,
        lastNameInitial: String,
        birthDay: String,
        birthMonth: String
    ): List<String>
}

class PersonalDetailsUtilImpl(private val passportMonths: List<String>) : PersonalDetailsUtil {

    override fun getPersonalDetails(
        firstNameInitial: String,
        lastNameInitial: String,
        birthDay: String,
        birthMonth: String
    ): List<String> {
        val birthDayReadableString = try {
            String.format("%02d", birthDay.toInt())
        } catch (e: Exception) {
            birthDay
        }

        val birthMonthReadableString = try {
            passportMonths[birthMonth.toInt() - 1]
        } catch (e: Exception) {
            birthMonth
        }

        return listOf(
            firstNameInitial,
            lastNameInitial,
            birthDayReadableString,
            birthMonthReadableString
        )
    }

}
