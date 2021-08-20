package nl.rijksoverheid.ctr.shared.utils

import nl.rijksoverheid.ctr.shared.models.PersonalDetails

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
        birthMonth: String,
        includeBirthMonthNumber: Boolean = false
    ): PersonalDetails
}

const val HIDDEN_PERSONAL_DETAIL = "_"

class PersonalDetailsUtilImpl(private val passportMonths: List<String>) : PersonalDetailsUtil {

    override fun getPersonalDetails(
        firstNameInitial: String,
        lastNameInitial: String,
        birthDay: String,
        birthMonth: String,
        includeBirthMonthNumber: Boolean
    ): PersonalDetails {
        val birthDayReadableString = try {
            String.format("%02d", birthDay.toInt())
        } catch (e: Exception) {
            birthDay
        }

        val birthMonthReadableString = try {
            val withoutNumber = passportMonths[birthMonth.toInt() - 1]
            if (includeBirthMonthNumber) {
                "$withoutNumber (${String.format("%02d", birthMonth.toInt())})"
            } else {
                withoutNumber
            }
        } catch (e: Exception) {
            birthMonth
        }

        return PersonalDetails(
            firstNameInitial = if (firstNameInitial.isEmpty()) HIDDEN_PERSONAL_DETAIL else firstNameInitial,
            lastNameInitial = if (lastNameInitial.isEmpty()) HIDDEN_PERSONAL_DETAIL else lastNameInitial,
            birthDay = if (birthDayReadableString.isEmpty()) HIDDEN_PERSONAL_DETAIL else birthDayReadableString,
            birthMonth = if (birthMonthReadableString.isEmpty()) HIDDEN_PERSONAL_DETAIL else birthMonthReadableString
        )
    }

}
