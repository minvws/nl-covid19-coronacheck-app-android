/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import org.junit.Test
import kotlin.test.assertEquals

class MultipleQrCodesUtilImplTest {

    @Test
    fun `2 of 2 not hidden qr should be most relevant above all`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "1", isHidden = false),
            createVaccination(dose = "3", totalDoses = "3", isHidden = false),
            createVaccination(dose = "2", totalDoses = "2", isHidden = false), // most relevant
            createVaccination(dose = "3", totalDoses = "2", isHidden = true)
        )

        assertEquals(util.getMostRelevantQrCodeIndex(vaccinations), 2)
    }

    @Test
    fun `The visible qr where dose and total dose is equal and highest is the most relevant`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "1", isHidden = false),
            createVaccination(dose = "3", totalDoses = "3", isHidden = false), // most relevant
            createVaccination(dose = "2", totalDoses = "2", isHidden = true),
            createVaccination(dose = "4", totalDoses = "5", isHidden = false)
        )

        assertEquals(util.getMostRelevantQrCodeIndex(vaccinations), 1)
    }

    @Test
    fun `When there is no equal doses visible qr, the qr with highest dose is most relevant`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "5", isHidden = false),
            createVaccination(dose = "2", totalDoses = "2", isHidden = true),
            createVaccination(dose = "5", totalDoses = "2", isHidden = false), // most relevant
            createVaccination(dose = "4", totalDoses = "5", isHidden = false)
        )

        assertEquals(util.getMostRelevantQrCodeIndex(vaccinations), 2)
    }

    private fun createVaccination(dose: String, totalDoses: String, isHidden: Boolean) =
        QrCodeData.European.Vaccination(
            dose = dose,
            ofTotalDoses = totalDoses,
            isHidden = isHidden,
            bitmap = mockk(),
            readEuropeanCredential = mockk()
        )
}