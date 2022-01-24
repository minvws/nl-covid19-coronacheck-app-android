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
    fun `The most relevant QR is the one with the highest dose which is not hidden`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "1", isHidden = false),
            createVaccination(dose = "3", totalDoses = "3", isHidden = false), // most relevant
            createVaccination(dose = "2", totalDoses = "2", isHidden = false),
            createVaccination(dose = "4", totalDoses = "2", isHidden = true)
        )

        assertEquals(util.getMostRelevantQrCodeIndex(vaccinations), 1)
    }

    @Test
    fun `The most relevant QR of highest equal doses is the one with the highest of total doses`() {
        val util = MultipleQrCodesUtilImpl()
        val vaccinations = listOf(
            createVaccination(dose = "1", totalDoses = "1", isHidden = false),
            createVaccination(dose = "3", totalDoses = "2", isHidden = false),
            createVaccination(dose = "3", totalDoses = "3", isHidden = false), // most relevant
            createVaccination(dose = "4", totalDoses = "5", isHidden = true)
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