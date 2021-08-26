package nl.rijksoverheid.ctr.shared.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PersonalDetailsUtilImplTest {

    private val personalDetailsUtil = PersonalDetailsUtilImpl(
        listOf("JAN", "FEB", "MAR")
    )

    @Test
    fun `getPersonalDetails returns correct personal details in order with valid date inputs`() {
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = "B",
            lastNameInitial = "N",
            birthDay = "2",
            birthMonth = "3"
        )
        assertEquals("B", personalDetails.firstNameInitial)
        assertEquals("N", personalDetails.lastNameInitial)
        assertEquals("02", personalDetails.birthDay)
        assertEquals("MAR", personalDetails.birthMonth)
    }

    @Test
    fun `getPersonalDetails returns correct personal details in order with valid date inputs and includes birth month number`() {
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = "B",
            lastNameInitial = "N",
            birthDay = "2",
            birthMonth = "3",
            includeBirthMonthNumber = true
        )
        assertEquals("B", personalDetails.firstNameInitial)
        assertEquals("N", personalDetails.lastNameInitial)
        assertEquals("02", personalDetails.birthDay)
        assertEquals("MAR (03)", personalDetails.birthMonth)
    }

    @Test
    fun `getPersonalDetails returns correct personal details in order with invalid date inputs`() {
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = "B",
            lastNameInitial = "N",
            birthDay = "X",
            birthMonth = "X"
        )
        assertEquals("B", personalDetails.firstNameInitial)
        assertEquals("N", personalDetails.lastNameInitial)
        assertEquals("X", personalDetails.birthDay)
        assertEquals("X", personalDetails.birthMonth)
    }

    @Test
    fun `getPersonalDetails returns correct personal details in order fields are empty`() {
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = "",
            lastNameInitial = "",
            birthDay = "",
            birthMonth = ""
        )
        assertEquals(HIDDEN_PERSONAL_DETAIL, personalDetails.firstNameInitial)
        assertEquals(HIDDEN_PERSONAL_DETAIL, personalDetails.lastNameInitial)
        assertEquals(HIDDEN_PERSONAL_DETAIL, personalDetails.birthDay)
        assertEquals(HIDDEN_PERSONAL_DETAIL, personalDetails.birthMonth)
    }

}
