package nl.rijksoverheid.ctr.shared.utils

import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtilImpl
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
        assertEquals("B", personalDetails[0])
        assertEquals("N", personalDetails[1])
        assertEquals("02", personalDetails[2])
        assertEquals("MAR", personalDetails[3])
    }

    @Test
    fun `getPersonalDetails returns correct personal details in order with invalid date inputs`() {
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = "B",
            lastNameInitial = "N",
            birthDay = "X",
            birthMonth = "X"
        )
        assertEquals("B", personalDetails[0])
        assertEquals("N", personalDetails[1])
        assertEquals("X", personalDetails[2])
        assertEquals("X", personalDetails[3])
    }

    @Test
    fun `getPersonalDetails returns correct personal details in order fields are empty`() {
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = "",
            lastNameInitial = "",
            birthDay = "",
            birthMonth = ""
        )
        assertEquals("_", personalDetails[0])
        assertEquals("_", personalDetails[1])
        assertEquals("_", personalDetails[2])
        assertEquals("_", personalDetails[3])
    }

}
