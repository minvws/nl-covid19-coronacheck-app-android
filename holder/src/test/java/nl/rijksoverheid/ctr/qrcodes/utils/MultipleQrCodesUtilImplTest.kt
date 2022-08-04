/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrcodes.utils

import io.mockk.mockk
import kotlin.test.assertEquals
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeData
import nl.rijksoverheid.ctr.holder.qrcodes.utils.MultipleQrCodesUtilImpl
import org.junit.Test

class MultipleQrCodesUtilImplTest {

    @Test
    fun `The most relevant QR is the one with the highest dose which is not hidden`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "1", isDoseNumberSmallerThanTotalDose = false),
            createVaccination(dose = "3", totalDoses = "3", isDoseNumberSmallerThanTotalDose = false), // most relevant
            createVaccination(dose = "2", totalDoses = "2", isDoseNumberSmallerThanTotalDose = false),
            createVaccination(dose = "4", totalDoses = "2", isDoseNumberSmallerThanTotalDose = true)
        )

        assertEquals(util.getMostRelevantQrCodeIndex(vaccinations), 1)
    }

    @Test
    fun `The most relevant QR of highest equal doses is the one with the highest of total doses`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "1", isDoseNumberSmallerThanTotalDose = false),
            createVaccination(dose = "3", totalDoses = "2", isDoseNumberSmallerThanTotalDose = false),
            createVaccination(dose = "3", totalDoses = "3", isDoseNumberSmallerThanTotalDose = false), // most relevant
            createVaccination(dose = "4", totalDoses = "5", isDoseNumberSmallerThanTotalDose = true)
        )

        assertEquals(util.getMostRelevantQrCodeIndex(vaccinations), 2)
    }

    private fun createVaccination(dose: String, totalDoses: String, isDoseNumberSmallerThanTotalDose: Boolean) =
        QrCodeData.European.Vaccination(
            dose = dose,
            ofTotalDoses = totalDoses,
            isDoseNumberSmallerThanTotalDose = isDoseNumberSmallerThanTotalDose,
            isExpired = false,
            bitmap = mockk(),
            readEuropeanCredential = mockk()
        )
}
